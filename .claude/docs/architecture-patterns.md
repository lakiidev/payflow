# PayFlow — Architecture Patterns

## Outbox Pattern

**Problem it solves:** Without outbox, a crash between `session.commit()` and `kafkaTemplate.send()` permanently loses the event. Money moved in the DB, but nothing downstream ever knows.

**How it works:** Write the domain event to an `outbox_events` table inside the same DB transaction as the state change. A `@Scheduled` relay reads unprocessed outbox rows and publishes them to Kafka in a separate transaction (`REQUIRES_NEW`). The relay marks rows as processed only after Kafka confirms receipt.

**✅ Good**
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void handle(InitiatePaymentCommand cmd) {
    Payment payment = Payment.initiate(cmd);
    eventStore.save(payment.domainEvents());

    // Written atomically with the state change — one transaction, one commit
    outboxRepository.save(OutboxEvent.from(new PaymentInitiated(payment.id())));
}

// Relay runs separately — REQUIRES_NEW so each batch is its own tx
@Scheduled(fixedDelay = 500)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void relay() {
    List<OutboxEvent> batch = outboxRepository.findUnprocessed(100);
    batch.forEach(event -> kafkaTemplate.send("payments", event.payload()));
    outboxRepository.markProcessed(batch);
}
```

**❌ Bad**
```java
@Transactional
public void handle(InitiatePaymentCommand cmd) {
    Payment payment = Payment.initiate(cmd);
    eventStore.save(payment.domainEvents());
    // DB transaction commits here

    // Crash here = event lost permanently. No retry, no recovery.
    kafkaTemplate.send("payments", new PaymentInitiated(payment.id()));
}
```

---

## CQRS (Command Query Responsibility Segregation)

**Problem it solves:** Write operations need SERIALIZABLE isolation and rich domain logic for correctness. Read operations need speed. Putting them in the same class/model means applying the strictest constraints everywhere — unnecessary lock contention and complexity on the read path.

**How it works:** Strict separation between write side (`commandhandler/`) and read side (`query/`). They use different transaction configs, different models, and different tables. The read model (projections) is updated by consuming domain events from Kafka — eventually consistent, but fast.

**✅ Good**
```java
// Write side — correctness, SERIALIZABLE, domain model
@Service
public class PaymentCommandHandler {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void handle(InitiatePaymentCommand cmd) {
        Payment payment = Payment.initiate(cmd);
        eventStore.save(payment.domainEvents());
        outboxRepository.save(OutboxEvent.from(new PaymentInitiated(payment.id())));
    }
}

// Read side — speed, readOnly, denormalized projection table
@Service
public class PaymentQueryHandler {
    @Transactional(readOnly = true)
    public TransactionHistoryDTO getHistory(UUID accountId) {
        return transactionProjectionRepository.findByAccountId(accountId);
    }
}
```

**❌ Bad**
```java
// One class handling both — SERIALIZABLE applied to reads for no reason,
// domain model exposed to the read path, no separation of concerns
@Service
public class PaymentService {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PaymentEntity initiatePayment(InitiatePaymentCommand cmd) { ... }

    @Transactional(isolation = Isolation.SERIALIZABLE) // pointless on a read
    public PaymentEntity getPayment(UUID id) {
        return paymentRepository.findById(id).orElseThrow();
    }
}
```

---

## Idempotent Consumer

**Problem it solves:** Kafka guarantees at-least-once delivery. After a consumer crash, rebalance, or timeout, the broker redelivers the last unacknowledged message. Without idempotency: double debit, double credit, double refund.

**How it works:** Before processing an event, check `processed_events` for the event ID. The check, the domain logic, and the insert into `processed_events` all happen inside one transaction. Kafka ack is sent only after the transaction commits.

**✅ Good**
```java
@KafkaListener(topics = "payments", groupId = "payflow-consumers")
public void consume(ConsumerRecord<String, PaymentInitiated> record, Acknowledgment ack) {
    transactionTemplate.execute(status -> {
        String eventId = record.value().eventId();

        // Check, process, and record — all in one transaction
        if (processedEventRepository.existsByEventId(eventId)) {
            return null; // already handled — skip silently
        }

        applyPaymentInitiated(record.value());
        processedEventRepository.save(new ProcessedEvent(eventId));
        return null;
    });

    ack.acknowledge(); // only after commit — safe to ack now
}
```

**❌ Bad**
```java
// enable-auto-commit: true in config (never acceptable in PayFlow)
@KafkaListener(topics = "payments")
public void consume(PaymentInitiated event) {
    // No idempotency check — redelivery = double processing
    applyPaymentInitiated(event);
    // Kafka auto-acks on return — acks before any persistence, crash = reprocess
}
```

---

## Saga (Choreography-Based)

**Problem it solves:** A cross-account transfer touches multiple aggregates. A traditional 2PC distributed transaction is fragile under partial failure and holds locks across services. Saga coordinates via compensating events instead of global rollback.

**How it works:** Each step in the flow publishes a domain event. The next service reacts to that event and publishes its own. On failure, a compensating event triggers the rollback of previous steps. No central orchestrator — services are decoupled.

**✅ Good**
```
PaymentInitiated →
    [Account Service]   FundsReserved | InsufficientFundsDetected →
    [Ledger Service]    LedgerEntriesPosted | LedgerPostingFailed →
    [Account Service]   FundsReleased  ← compensating tx if any step failed
    [Payment Service]   PaymentCompleted | PaymentFailed
```
Each step is independently retryable and idempotent. Failure triggers a compensating event chain — not a global lock release.

**❌ Bad**
```java
// Synchronous orchestration — one failure breaks everything, locks held across calls
public void transfer(TransferCommand cmd) {
    accountService.debit(cmd.sourceId(), cmd.amount());   // HTTP call
    ledgerService.post(cmd);                               // HTTP call — if this throws:
    accountService.credit(cmd.targetId(), cmd.amount());  // never reached, money gone
}
```

---

## Double-Entry Ledger

**Problem it solves:** A mutable `balance` column can desync from reality with no trace of how it happened. There's no audit trail, no way to reconstruct history, and no way to detect corruption.

**How it works:** Every money movement creates two rows in `ledger_entries` — a debit on one account and a credit on another. Balance is never stored; it's always derived by summing ledger entries for an account. Every cent is traceable to a payment.

**✅ Good**
```sql
CREATE TABLE ledger_entries (
    id           UUID PRIMARY KEY,
    account_id   UUID NOT NULL,
    amount_cents BIGINT NOT NULL,         -- positive = credit, negative = debit
    entry_type   VARCHAR(10) NOT NULL,    -- 'DEBIT' | 'CREDIT'
    payment_id   UUID NOT NULL REFERENCES payments(id),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Balance is always derived — never stale, never wrong without an explanation
SELECT SUM(amount_cents) FROM ledger_entries WHERE account_id = $1;
```

```java
// Both entries written in the same transaction as the payment
public void postLedgerEntries(Payment payment) {
    ledgerRepository.save(LedgerEntry.debit(payment.sourceAccountId(), payment.amountCents(), payment.id()));
    ledgerRepository.save(LedgerEntry.credit(payment.targetAccountId(), payment.amountCents(), payment.id()));
}
```

**❌ Bad**
```sql
-- Direct mutation — no audit trail, can go negative silently, can desync
UPDATE accounts SET balance_cents = balance_cents - 5000 WHERE id = $1;
UPDATE accounts SET balance_cents = balance_cents + 5000 WHERE id = $2;
-- If second UPDATE crashes: 5000 cents vanished with no record
```

---

## Event Sourcing

**Problem it solves:** Point-in-time state tables lose history. If a projection has a bug that corrupts read data, there's no way to rebuild it. Financial systems require a complete, immutable audit trail.

**How it works:** `event_store` is append-only and is the single source of truth. Aggregates are reconstructed by replaying all their events. Projection tables are derived views — they can always be dropped and rebuilt by replaying the event store.

**✅ Good**
```java
// Aggregate loaded by replaying its full event history
public Payment load(UUID paymentId) {
    List<DomainEvent> events = eventStore.findByAggregateId(paymentId);
    if (events.isEmpty()) throw new PaymentNotFoundException(paymentId);
    return Payment.reconstitute(events);
}

// Aggregate accumulates events, never mutates external state directly
public class Payment {
    public static Payment reconstitute(List<DomainEvent> events) {
        Payment payment = new Payment();
        events.forEach(payment::apply);
        return payment;
    }

    private void apply(PaymentInitiated event) {
        this.id = event.paymentId();
        this.status = PaymentStatus.PENDING;
        this.amountCents = event.amountCents();
    }
}
```

**❌ Bad**
```java
// Snapshot table only — history is gone, projections can't be rebuilt
public Payment load(UUID paymentId) {
    return paymentRepository.findById(paymentId)
        .orElseThrow(() -> new PaymentNotFoundException(paymentId));
}

// State is overwritten, not appended
public void complete(UUID paymentId) {
    Payment payment = load(paymentId);
    payment.setStatus("COMPLETED");     // history of how it got here: gone
    paymentRepository.save(payment);
}
```

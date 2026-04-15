# ADR-013: Outbox Pattern over Direct Kafka Publish

## Status
Accepted — Week 2

## Context
After a transaction is written to PostgreSQL, downstream consumers must be
notified via a Kafka event. The write to PostgreSQL and the publish to Kafka
are two separate operations against two separate systems. Either can fail
independently.

The question is how to guarantee that every committed transaction produces
exactly one Kafka event — no lost events, no phantom events for rolled-back
transactions.

## Decision
Transaction events are written to an `outbox_events` table in the same
database transaction as the transaction write. A separate `OutboxRelay`
scheduler polls the outbox table, publishes pending events to Kafka, and
marks them as published. Kafka is never written to inside a database
transaction.

## Alternatives Considered

**Direct Kafka publish inside @Transactional**
- `kafkaTemplate.send()` is called inside the same `@Transactional` method
  as the database write
- If the database commit succeeds but Kafka is unavailable, the event is lost —
  the transaction is committed with no corresponding Kafka event
- If Kafka succeeds but the database rolls back, a phantom event is published
  for a transaction that never existed
- No atomicity guarantee between the two systems — dual-write problem

**Direct Kafka publish after @Transactional via @TransactionalEventListener**
- Event is published after the database transaction commits successfully —
  eliminates phantom events for rolled-back transactions
- If Kafka is unavailable at publish time, the event is silently lost —
  no retry, no record that the publish was attempted
- Acceptable only if event loss is tolerable — not acceptable for a
  financial audit trail

**Outbox pattern (chosen)**
- `OutboxEvent` is written to `outbox_events` in the same `@Transactional`
  as the transaction write — atomicity guaranteed by PostgreSQL
- If the database transaction rolls back, the outbox entry rolls back with it —
  no phantom events
- `OutboxRelay` polls `outbox_events WHERE status = PENDING` on a fixed
  schedule and publishes to Kafka
- On successful Kafka publish, the outbox entry is marked `PUBLISHED`
- If Kafka is unavailable, the outbox entry remains `PENDING` and is retried
  on the next polling cycle — no event loss
- The relay is the only component that writes to Kafka — clean separation
  between the write path and event delivery

## Rationale
The dual-write problem is fundamental: two systems cannot be updated atomically
without a distributed transaction. Distributed transactions (XA/2PC) are
complex, slow, and unsupported by most Kafka clients.

The Outbox pattern sidesteps distributed transactions by reducing the problem
to a single atomic operation — writing to PostgreSQL — and making Kafka
delivery a separate, retryable concern. The outbox table acts as a durable
queue within the database, leveraging PostgreSQL's ACID guarantees to ensure
no event is lost between the business write and Kafka delivery.

The tradeoff is operational complexity: the relay is an additional component
that must be monitored, and event delivery is eventually consistent rather
than immediate. In a financial system, guaranteed delivery with a small delay
is preferable to immediate delivery with possible loss.

## Consequences
- `outbox_events` table: `id`, `aggregate_id`, `aggregate_type`, `event_type`,
  `payload JSONB`, `status`, `created_at`
- `TransactionOutboxWriter` writes to `outbox_events` inside the command
  handler's `@Transactional` — no direct Kafka dependency in the write path
- `OutboxRelay` is a `@Scheduled` bean polling every 5 seconds with
  `@Transactional(propagation = REQUIRES_NEW)` to isolate each publish attempt
- Kafka publish failures do not affect the write path — the command handler
  has already committed before the relay runs
- Event delivery is eventually consistent — consumers may lag behind the
  write path by up to one polling interval
- `outbox_events` must be pruned periodically — entries older than 7 days
  with status `PUBLISHED` can be deleted safely
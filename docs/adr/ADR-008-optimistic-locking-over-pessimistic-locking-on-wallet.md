# ADR-008: Optimistic locking over pessimistic locking on Wallet

## Status
Accepted — Week 1 (credit/debit implementation deferred to Week 3)

## Context
`Wallet` is the central aggregate in the system. Every credit and debit operation
performs a read-modify-write on `current_balance` — a classic concurrency
hazard. Without a locking strategy, two concurrent transactions can read the
same balance, both apply their modification, and one update silently overwrites
the other (lost update anomaly).

Two standard strategies exist: optimistic locking (detect conflicts at commit
time) and pessimistic locking (prevent concurrent access at read time).

## Decision
Use `@Version` optimistic locking on `Wallet`. Hibernate manages the version
column — every write increments it, and a stale version at commit time throws
`ObjectOptimisticLockingFailureException`, mapped to HTTP 409.

## Alternatives Considered

**Pessimistic locking (`SELECT FOR UPDATE`)**
- Locks the wallet row at read time — no concurrent transaction can read or
  write until the lock is released
- Guarantees no conflict — but at the cost of throughput
- Long-running transactions hold the lock for their entire duration
- Risk of deadlock when multiple wallets are involved in one transaction
- Appropriate when conflict rate is high and retry cost is prohibitive

**Optimistic locking (`@Version`) (chosen)**
- No lock held during the transaction — full read concurrency
- Conflict detected at commit time via version check
- Failed commit throws `ObjectOptimisticLockingFailureException` — client
  retries the operation
- Higher throughput under low-to-moderate contention
- Appropriate when conflict rate is low and retry cost is acceptable

## Rationale
A payment system has high read concurrency but relatively low write contention
per individual wallet. Two transactions targeting the exact same wallet at the
exact same millisecond is the exception, not the rule. Pessimistic locking
would serialize all writes to a wallet regardless of whether a conflict would
actually occur — killing throughput for a problem that rarely materializes.

Optimistic locking pays its cost only when a conflict actually happens. The
client retries, which is the correct behavior for a transient concurrency
failure. This is the standard approach for financial aggregates at PayFlow's
scale.

SERIALIZABLE isolation (see ADR-011) handles the broader anomaly prevention.
`@Version` handles the lost update hazard specifically on `Wallet` writes.
The two work together.

## Consequences
- `wallets` table has a `version` column managed by Hibernate
- Concurrent modifications to the same wallet result in a 409 — not a 500
- Clients must implement retry logic for 409 on wallet operations
- `ObjectOptimisticLockingFailureException` is mapped to HTTP 409 in the
  global exception handler
- Under high contention on a single wallet, retry storms are theoretically
  possible — acceptable at PayFlow's scale
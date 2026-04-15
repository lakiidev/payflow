# ADR-011: Denormalize user_id on Transactions for Read Performance

## Status
Accepted — Week 2

## Context
The `transactions` table has no direct reference to `user_id`. Ownership
is derived through `wallets` — a transaction belongs to a user only because
its `from_wallet_id` or `to_wallet_id` references a wallet owned by that user.

The read side requires scoping all transaction queries to the authenticated
user: `GET /transactions` must return only transactions belonging to the
requesting user. Without a direct `user_id` on `transactions`, this requires
a join through `wallets` on every read.

## Decision
A `user_id` column is added to the `transactions` table via a Flyway migration.
It is populated at write time by the command handler, which has the authenticated
user's ID available from the JWT principal. Read queries scope directly to
`WHERE user_id = :userId` with no join.

## Alternatives Considered

**Join through wallets on every read (3NF canonical)**
- No schema change required — `user_id` is derivable through `wallets`
- Every read executes a subquery or join:
  `WHERE from_wallet_id IN (SELECT id FROM wallets WHERE user_id = :userId)
   OR to_wallet_id IN (SELECT id FROM wallets WHERE user_id = :userId)`
- Correct in a normalized schema, expensive at read volume
- OR condition prevents efficient index usage — both sides must be evaluated
- Adds complexity to every query that needs user-scoped transactions

**Projection table with user_id (pure CQRS)**
- Separate `transaction_projections` table populated by Kafka consumers
- Fully decoupled read model — write side schema changes do not affect reads
- Requires Kafka consumers to be running before any reads are possible
- Adds operational dependency: read side is unavailable if consumers are behind
- Appropriate for Week 3 when the full CQRS read split with read replica is introduced
- Deferred to Week 3 — consumer infrastructure is not complete in Week 2

**Denormalize user_id onto transactions (chosen)**
- Single additional column — minimal schema change
- `user_id` is immutable: a transaction never changes ownership once created
- No update anomalies are possible — the denormalization is safe by domain invariant
- Read query is `WHERE user_id = :userId` with a single indexed column lookup
- `idx_tx_user_id` index makes pagination over large transaction sets efficient
- Bridges the gap between Week 2 (no read replica) and Week 3 (full CQRS split)

## Rationale
Strict normalization prescribes deriving `user_id` through `wallets`. In a
financial read path serving paginated transaction history, a cross-table OR
join on every request is not acceptable. The join grows more expensive as
`ledger_entries` and `transactions` accumulate over the wallet lifetime.

Denormalization is safe here because `user_id` on a transaction is immutable —
a transaction cannot be reassigned to a different user. There is no scenario
where the denormalized value could drift from the normalized derivation.
The risk that normally motivates avoiding denormalization does not apply.

In Week 3, if a full projection table is introduced as the CQRS read model,
the `user_id` column on `transactions` becomes redundant but harmless. The
migration cost of removing it is low; the cost of the join it replaces is not.

## Consequences
- `V12__Add_userId_to_transactions.sql` adds `user_id UUID REFERENCES users(id)`
  and `CREATE INDEX idx_tx_user_id ON transactions(user_id) via `V13__create_user_id_index_transactions`
- Command handlers populate `user_id` at write time from the authenticated principal
- `TransactionRepository` exposes `findAllByUserId(UUID userId, Pageable pageable)`
  and `findByIdAndUserId(UUID id, UUID userId)` — no joins required
- Ownership enforcement is at the query level — a user cannot retrieve another
  user's transactions even if they know the transaction ID
- If a projection table is introduced in Week 3, this column remains in place
  and the projection table takes over as the primary read model
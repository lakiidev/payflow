# ADR-004: Wallet balance as cached field, ledger as source of truth

## Status
Accepted — Week 1 (credit/debit implementation deferred to Week 3)

## Context
System plans to use a double-entry ledger to record every financial movement as
immutable `LedgerEntry` rows. (see why in future ADR) This raises a question: when the current balance
of a wallet is needed, should it be derived by summing ledger entries at query
time, or maintained as a cached field on the `Wallet` entity itself?

Balance reads are frequent — every transaction, every balance check, every
UI render. The choice affects read performance, data consistency guarantees,
and the complexity of the write path.

## Decision
Maintain `current_balance` as a cached field on the `Wallet` entity, updated
atomically with each credit or debit. The ledger remains the source of truth —
`current_balance` is always derivable by summing the wallet's ledger entries.

## Alternatives Considered

**Derive balance from ledger at query time**
- No cached state — balance is always mathematically correct by definition
- Eliminates any possibility of cache/ledger divergence
- Read performance degrades as ledger grows — full table scan per balance check
- Unacceptable for a payment system where balance reads are on the hot path

**Cached balance on Wallet (chosen)**
- `current_balance` updated atomically within the same transaction as the
  ledger entry write
- O(1) balance reads regardless of ledger size
- Requires `@Version` optimistic locking to prevent concurrent update anomalies
- Cache can theoretically diverge from ledger if a bug bypasses the domain model
  — mitigated by making direct balance mutation impossible outside `credit()`
  and `debit()`

## Rationale
It is not scalable to sum up ledger entries on each read. For a single balance check,
a wallet with years of transaction history would need to aggregate thousands of rows.
Financial systems typically keep an immutable ledger and a running balance for this reason.

The consistency risk is real but manageable. By making sure that `current_balance` can only 
be changed by `Wallet.credit()` and `Wallet.debit()`—never directly—and by writing the ledger 
entry and updating the balance in the same SERIALISABLE transaction, divergence can only happen
if there is a bug that completely skips the domain model. A periodic reconciliation job can detect and alert on any
divergence.

## Consequences
- Balance reads are O(1) — no ledger aggregation on the hot path
- `Wallet.credit()` and `Wallet.debit()` are the only mutation points —
  enforced by domain model design, not database constraints
- `@Version` optimistic locking on `Wallet` guards against lost updates
  under concurrent transactions (see ADR-008)
- SERIALIZABLE isolation on credit/debit handlers ensures ledger entry and
  balance update are atomic (see ADR-011)
- A reconciliation job comparing `current_balance` against summed ledger
  entries is deferred — frequency and alerting strategy to be decided




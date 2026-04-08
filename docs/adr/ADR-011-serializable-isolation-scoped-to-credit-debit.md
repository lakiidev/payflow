# ADR-011: SERIALIZABLE isolation scoped to credit/debit only, READ_COMMITTED for auth

## Status
Accepted — Week 1 (credit/debit implementation deferred to Week 3)

## Context
Spring's default transaction isolation level inherits from the database default,
which in PostgreSQL is READ COMMITTED. Different operations in system have
fundamentally different concurrency requirements — financial mutations on
`Wallet` require strong isolation guarantees, while authentication operations
are simple reads and writes with no meaningful concurrency hazard.

Applying SERIALIZABLE globally would provide maximum safety but at significant
throughput cost. The question is how to scope isolation levels appropriately
across different operation types.

## Decision
Use SERIALIZABLE isolation exclusively on credit and debit command handlers.
All other operations — authentication, wallet creation, balance reads — use
READ COMMITTED (PostgreSQL default).

## Alternatives Considered

**SERIALIZABLE globally**
- Maximum safety — no concurrency anomalies anywhere in the system
- Significant throughput cost — SSI conflict detection overhead on every
  transaction regardless of whether concurrency hazards exist
- Auth operations (login, register) have no meaningful concurrency hazard —
  SERIALIZABLE adds overhead with no safety benefit there
- Overkill for operations that are not performing read-modify-write on
  shared financial state

**READ COMMITTED globally**
- PostgreSQL default — minimal overhead, maximum throughput
- Insufficient for credit/debit — vulnerable to lost update anomaly on
  `current_balance` without additional protection
- `@Version` alone partially mitigates this but SERIALIZABLE provides
  stronger guarantees for financial mutations

**Scoped isolation (chosen)**
- SERIALIZABLE on `@Transactional` in credit/debit command handlers only
- READ COMMITTED everywhere else
- Isolation level matches the actual concurrency risk of each operation
- No unnecessary overhead on auth and read operations

## Rationale
Isolation levels should match the concurrency hazards of the operation, not
be applied uniformly for simplicity. Auth operations perform straightforward
inserts and lookups — there is no read-modify-write, no shared mutable state,
no meaningful anomaly to prevent. Applying SERIALIZABLE there wastes resources
and increases abort rates for no safety gain.

Credit and debit operations modify `current_balance` — shared mutable financial
state that multiple concurrent transactions can target simultaneously. This is
exactly the scenario SERIALIZABLE isolation exists for. Combined with `@Version`
optimistic locking (ADR-008), concurrent wallet mutations are both detected and
prevented at the isolation level.


## Consequences
- Credit/debit command handlers declare
  `@Transactional(isolation = Isolation.SERIALIZABLE)` explicitly
- All other `@Transactional` annotations use the default READ COMMITTED
- PostgreSQL uses SSI (Serializable Snapshot Isolation) for SERIALIZABLE —
  lower overhead than 2PL-based serializability but still non-zero
- Serialization failures on credit/debit result in transaction abort and
  retry — handled alongside `@Version` conflict retries
- Auth throughput is unaffected by SERIALIZABLE overhead
- Retry strategy for compounded SERIALIZABLE abort and @Version conflict
    is deferred — a retry budget (max attempts) and exponential backoff
    strategy will be defined in Week 3 when credit/debit handlers are
    implemented. Unbounded retries under contention are a known risk
# ADR-009: Hybrid DDD — aggregate model for Wallet, plain entity for User

## Status
Accepted — Week 1 (credit/debit implementation deferred to Week 3)

## Context
System's domain contains two primary entities: `Wallet` and `User`. Full DDD
prescribes that all domain objects are modeled as aggregates with rich behavior,
domain invariants enforced at the model level, and no framework imports in the
domain layer.

However, DDD purity has a cost — additional abstraction, more classes, and
stricter separation that only pays off when the protected domain object has
behavior worth isolating. The question is whether to apply full DDD uniformly
or selectively based on where the complexity actually lives.

## Decision
Apply full DDD aggregate modeling to `Wallet`. Treat `User` as a plain JPA
`@Entity` with no domain behavior. Future `LedgerEntry` and `AuditLog` are also
plain entities — append-only records with no behavior to protect.

## Alternatives Considered

**Full DDD across all entities**
- Consistent modeling — every entity follows the same pattern
- Domain repository interfaces, factory methods, and domain events everywhere
- `User` would have no domain invariants to enforce — abstraction with no payoff
- Significant boilerplate for entities that are essentially data records

**Plain JPA entities across all entities**
- Simple, pragmatic — all entities are `@Entity` with direct `JpaRepository`
- No domain layer separation — Spring Data and JPA leak everywhere
- Loses the invariant enforcement that makes `Wallet` safe under concurrency
- Ousterhout: shallow classes everywhere signal over-decomposition

**Hybrid DDD (chosen)**
- Full aggregate model where domain complexity exists (`Wallet`)
- Plain `@Entity` where it doesn't (`User`, `LedgerEntry`, `AuditLog`)
- Complexity matches the model — no abstraction without justification

## Rationale
`Wallet` has real domain invariants worth protecting:
- Balance must never go negative
- Mutations only through `credit()` and `debit()`
- Concurrency safety via `@Version`
- Domain events emitted on state change

`User` has none of these. It is a credential and identity record — an email,
a password hash, a role. There are no business rules that require protecting
`User` behind a domain model. Applying full DDD to `User` would produce a
shallow aggregate with no behavior, which Ousterhout identifies as the exact
failure mode of over-decomposition.

The hybrid approach follows the principle that architecture should match
complexity. Full DDD where the domain is rich, plain entities where it is not.

## Consequences
- `Wallet` enforces all invariants internally — no service layer can mutate
  balance directly
- `User` is a plain `@Entity` with Spring Security coupling acceptable
  (see ADR-003)
- `LedgerEntry` is append-only — no update or delete methods, enforced by
  the domain model
- If `User` grows domain behavior (e.g. account suspension rules, KYC state
  machine), it is promoted to a full aggregate at that point
- Inconsistent modeling across entities is a known tradeoff — justified by
  matching abstraction to actual complexity
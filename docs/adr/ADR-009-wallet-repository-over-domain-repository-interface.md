# ADR-009: WalletRepository over domain repository interface until unit tests justify it
## Status
Accepted — Week 1
To be refactored in Week 3 — WalletQueryHandlerTest already mocks
WalletRepository directly, confirming the domain interface is now justified.
Applies to all aggregate repositories: WalletRepository, TransactionRepository,
and LedgerEntryRepository — all three to be refactored together in Week 3.

## Context
DDD prescribes that aggregates are accessed through domain repository interfaces
defined in the domain layer, with infrastructure implementations in the
persistence layer. This keeps the domain free of JPA and Spring Data imports
and allows the repository to be mocked in unit tests without a database.

However, defining a domain repository interface adds a layer of indirection
that only pays off when unit tests actually mock it. The question is whether
to add this abstraction upfront or defer it until the tests that justify it
exist.

## Decision
`WalletRepository` extends `JpaRepository` directly in the persistence layer.
No domain repository interface. No separate infrastructure implementation class.
The repository is injected and used directly in command and query handlers.

## Alternatives Considered

**Domain repository interface (DDD canonical)**
- `WalletRepository` interface defined in the domain layer with no JPA imports
- `JpaWalletRepository` in the infrastructure layer implements it
- Command handlers depend on the domain interface — fully mockable in unit tests
- Correct long-term architecture for a DDD aggregate
- Adds two classes and an interface per aggregate with no immediate payoff
  if unit tests don't exist yet

**Direct JpaRepository (chosen)**
- `WalletRepository extends JpaRepository` lives in the persistence layer
- Injected directly into command and query handlers
- One class per aggregate — no indirection
- JPA leaks into the application layer as a compile-time dependency
- Justified while unit tests don't yet require mocking the repository
- Refactoring to a domain interface is mechanical when the time comes

## Rationale
Abstraction without a current use case is a speculative complexity. The domain
repository interface earns its existence when a unit test needs to mock it.
Until that test exists, the interface is indirection for its own sake.

Week 1 has no Testcontainers integration tests and no unit tests that require
mocking the repository. Adding the domain interface now would be building
infrastructure for a future that hasn't arrived. The refactor from
`WalletRepository extends JpaRepository` to a domain interface is mechanical
and low-risk — it does not need to happen before the tests that motivate it
exist.

## Consequences
- `WalletRepository` lives in the persistence layer — no domain interface,
  no infrastructure implementation class
- Spring Data JPA is a compile-time dependency of command and query handlers
  in Week 1
- Refactoring to domain repository interface is deferred to Week 3 alongside
  Testcontainers and unit test introduction
- The refactor is mechanical — extract interface, move to domain layer,
  update injection points
- All Week 1 repository usage goes through `JpaRepository` methods directly —
  no custom repository methods that would complicate the future extraction
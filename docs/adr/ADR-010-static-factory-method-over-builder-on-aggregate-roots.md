# ADR-010: Static factory method over @Builder on aggregate roots

## Status
Accepted — Week 1

## Context
Aggregate roots require controlled construction — invariants must be enforced
at the point of creation, not left to the caller to satisfy. Two common
approaches in Java are Lombok's `@Builder` and static factory methods.

The choice affects how strongly the aggregate can enforce its own invariants
at construction time and how readable the intent of construction is at the
call site.

## Decision
Use static factory methods (e.g. `Wallet.create()`) on aggregate roots.
`@Builder` is permitted on DTOs and value objects where invariant enforcement
is not a concern.

## Alternatives Considered

**Lombok @Builder**
- Generates a fluent builder — readable at the call site
- Any subset of fields can be set — caller decides which fields to populate
- No natural place to enforce invariants — validation must happen externally
  or in a separate method after construction
- A partially constructed aggregate can exist — `build()` does not guarantee
  a valid object
- Exposes internal field names as builder methods — couples callers to the
  internal structure of the aggregate

**Static factory method (chosen)**
- Construction intent is explicit — `Wallet.create(userId, currency)` reads
  as a domain operation, not object assembly
- Invariants enforced inside the method before the object is returned —
  no partially valid aggregate can escape
- Parameters are minimal and meaningful — only what is required to create
  a valid aggregate
- Default state (e.g. `currentBalance = ZERO`, `status = ACTIVE`) is set
  internally — callers cannot accidentally omit or override it

## Rationale
`@Builder` is a construction convenience, not a domain modeling tool. It
optimizes for flexibility at the call site at the cost of invariant safety.
For a DTO where any combination of fields is valid, that tradeoff is
acceptable. For an aggregate root where invalid state must be impossible,
it is not.

`Wallet.create(userId, currency)` enforces that every `Wallet` starts with
`currentBalance = ZERO` and `status = ACTIVE`. There is no way to construct
a `Wallet` with a non-zero opening balance or an invalid status through the
factory method. `@Builder` provides no such guarantee — a caller could
`builder().currentBalance(BigDecimal.valueOf(-100)).build()` and the aggregate
would accept it silently.

## Consequences
- Every aggregate root has one or more named factory methods reflecting
  domain intent
- Invalid aggregate state is impossible to construct from outside the class
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` used alongside the
  factory method to satisfy JPA while keeping the public constructor blocked
- `@Builder` remains available for DTOs, command objects, and value objects
  where invariant enforcement is not required
- Adding new required fields to an aggregate requires updating the factory
  method signature — a compile-time break that is intentional, not a drawback
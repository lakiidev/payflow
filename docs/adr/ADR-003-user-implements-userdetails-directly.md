# ADR-003: User implements UserDetails directly over a UserDetailsImpl wrapper

## Status
Accepted Week 1

## Context
`UserDetails` object is required for Sping to perform
Authentication. There are two standard approaches to
provide that: have a domain `User` entity implement
`USerDeatils` directly, or create a separate `UserDetailsImpl` wrapper
class that adapts `User` to the `UserDetails` interface.

## Decision
`User` implements `UserDetails` directly.

## Alternatives Considered

**UserDetails wrapper**
- A separate class implements `UserDetails` and has reference to a `User`
- Domain `User` without Security dependency
- Stricter DDD - infrastructure details don't leak into the domain model
- Requires extra class and mapping
- More boilerplate for no functional difference and advantage

**User implements UserDetails directly**
- `User` carries annotations from Spring Security
- One class less - `UserDetailsServiceConfig` returns the `User` directly
- Spring Security leaks into the domain layer as a compile-time dependency
- Pragmatic for a project where `User` is not a pure aggregate

## Rationale
`User` is intentionally a plain `@Entity` rather than a full DDD
aggregate — this was already established in ADR-009. Given that `User` carries
no domain invariants and no business logic worth protecting from framework
coupling, the strict separation a wrapper provides has no practical payoff here.

The wrapper pattern earns its complexity when the domain `User` has rich
behavior that must be tested in isolation from Spring Security. PayFlow's `User`
does not — it is essentially a credential and identity record. Adding
`UserDetailsImpl` would be indirection for its own sake.

This is revisited if `User` grows domain behavior that warrants isolation.

## Consequences
- Spring Security is a compile-time dependency of the domain `User` class
- `UserDetailsServiceConfig` is simpler — loads `User` from the repository
  and returns it directly
- Unit testing `User` requires Spring Security on the classpath
- Acceptable tradeoff given `User`'s role as a plain entity in this architecture
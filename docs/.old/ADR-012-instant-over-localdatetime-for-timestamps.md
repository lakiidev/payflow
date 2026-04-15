# ADR-012: Instant over LocalDateTime for all timestamps

## Status
Accepted — Week 1

## Context
Every entity in PayFlow records timestamps for creation and modification.
Java offers multiple types for representing time — the two most common in
Spring Boot applications are `LocalDateTime` (timezone-naive) and `Instant`
(absolute point in time, UTC-based).

Financial systems are particularly sensitive to timestamp correctness —
audit trails, ledger entries, and transaction ordering all depend on
unambiguous time representation.

## Decision
Use `Instant` for all timestamp fields across all entities. PostgreSQL
columns are mapped as `TIMESTAMPTZ` (timestamp with time zone).

## Alternatives Considered

**LocalDateTime**
- Human-readable, familiar to most Java developers
- No timezone information — represents a wall clock time with no UTC offset
- Stored as `TIMESTAMP WITHOUT TIME ZONE` in PostgreSQL by default
- Ambiguous during DST transitions — the same `LocalDateTime` can represent
  two different absolute moments
- Dangerous in a financial system where timestamp ordering must be
  unambiguous across timezones

**Instant (chosen)**
- Represents an absolute point in time — always UTC, no timezone ambiguity
- Maps to `TIMESTAMPTZ` in PostgreSQL — timezone-aware storage
- Unambiguous ordering — two `Instant` values always have a clear before/after
  relationship
- Correct type for audit trails, ledger timestamps, and transaction records
- Slight reduction in human readability — mitigated by formatting at the
  API response layer

## Rationale
`LocalDateTime` is the wrong type for a financial system. A timestamp without
timezone information cannot unambiguously represent when something happened —
it depends on the context of whoever reads it. For a ledger entry or audit
record, that ambiguity is unacceptable.

`Instant` removes the ambiguity entirely. Every timestamp is an absolute UTC
moment. Ordering is deterministic regardless of where the server runs or
where the client reads from. DST transitions cannot corrupt the audit trail.

Formatting for human consumption happens at the API layer — `Instant` is
serialized to ISO-8601 in responses, keeping the domain model correct and
the API readable.

## Consequences
- All `createdAt` and `updatedAt` fields are `Instant` across all entities
- All timestamp columns in Flyway migrations are defined as `TIMESTAMPTZ`,
  never `TIMESTAMP` — enforced by convention across all migration files
- `@CreationTimestamp` and `@UpdateTimestamp` populate `Instant` fields
  correctly via Hibernate
- API responses serialize `Instant` as ISO-8601 strings —
  e.g. `2025-04-07T10:15:30Z`
- No timezone conversion logic needed in the domain or persistence layer
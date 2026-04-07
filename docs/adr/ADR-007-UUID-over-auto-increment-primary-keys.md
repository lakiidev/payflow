# ADR-007: UUID over auto-increment primary keys

## Status
Accepted — Week 1

## Context
Every entity in the system requires a primary key strategy. The two standard
options are auto-increment integers (sequential, database-generated) and UUIDs
(universally unique, generatable anywhere). The choice affects key predictability,
distribution safety, and application-layer ID generation.

Financial systems have additional considerations — sequential IDs expose
business intelligence (transaction volume, user count) and are trivially
enumerable by an attacker.

## Decision
Use UUID v4 for all primary keys, generated at object construction time in Java via Hibernate's @UuidGenerator . Mapped as `UUID` in Java, stored as native `uuid` type
in PostgreSQL.

## Alternatives Considered

**Auto-increment integers**
- Simple, human-readable, naturally ordered
- Sequential — exposes record counts and growth rate to anyone who can observe
  IDs
- Enumerable — an attacker can iterate `GET /api/wallets/1`, `/2`, `/3`
- Tightly coupled to the database — ID only exists after an insert
- Problematic in future distributed or multi-database scenarios

**UUID v4 (chosen)**
- Non-sequential, non-enumerable — no business intelligence leakage
- Globally unique — safe across databases, environments, and future shards
- Can be generated before the database insert — useful for Outbox pattern
  where the aggregate ID must be known before persistence
- Native `uuid` type in PostgreSQL — no storage penalty over a string
- No meaningful human-readability loss in a system where IDs are never
  user-facing

## Rationale
Sequential IDs are a liability in a financial system. Exposing user count
or transaction volume through observable IDs is an information leak that
has no upside. UUID v4 eliminates this entirely.

The more important reason for PayFlow specifically is the Outbox pattern
(see future ADR). The Outbox requires embedding the aggregate ID in the domain
event before the transaction commits. Auto-increment IDs are not available
until after the insert — UUIDs can be assigned at object construction time,
making them a natural fit for event-driven architectures.

`@UuidGenerator` generates the UUID in the Java domain object before
persistence — the ID exists at construction time, satisfying the Outbox
requirement of knowing the aggregate ID before the transaction commits.

## Consequences
- Primary keys are non-enumerable — direct object access requires a valid UUID
- IDs are available at object construction time — compatible with Outbox
  pattern and domain event design
- No natural sort order by insertion time — use `created_at` for ordering,
  not the ID
- UUIDs are 128-bit — slightly larger than 64-bit bigint, negligible at
  PayFlow's scale
- Human debugging is slightly harder — mitigated by always logging
  correlation IDs alongside UUIDs
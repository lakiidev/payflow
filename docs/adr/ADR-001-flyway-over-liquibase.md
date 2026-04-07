# ADR-001: Flyway over Liquibase for Schema Migrations

## Status
Accepted — Week 1 (Implementation of Kafka deferred for Week 2)

## Context
PayFlow requires a database migration tool to manage PostgreSQL schema evolution
across environments (local Docker, CI, production). The two dominant
options in the Spring Boot ecosystem are Flyway and Liquibase.

Schema migrations in a financial system are high-stakes — incorrect or
partially applied migrations can corrupt ledger data or break double-entry
invariants. The tool must be reliable, auditable, and operationally simple.

## Decision
Use Flyway with SQL-based migrations (`V{version}__{description}.sql`).
Hibernate `ddl-auto` is set to `validate` — Flyway owns the schema entirely,
Hibernate never touches DDL.

## Alternatives Considered

**Liquibase**
- Supports XML, YAML, JSON, and SQL changelog formats
- More expressive rollback support out of the box
- Higher conceptual surface area — changelogs, changesets, contexts, labels
- XML/YAML format adds abstraction between intent and actual SQL

**Flyway**
- Pure SQL migrations — what you write is exactly what runs
- Linear versioning (`V1`, `V2`, ...) enforces a clear migration history
- Simpler mental model with less configuration
- Native Spring Boot autoconfiguration with minimal setup
- Checksum validation catches accidental migration edits

## Rationale
For a financial system, **auditability and predictability outweigh flexibility**.
Flyway's SQL-first approach means migrations are readable by anyone with SQL
knowledge, diff cleanly in Git, and leave no ambiguity about what ran against
the database.

Liquibase's rollback support is its strongest argument, but automatic rollbacks
in a financial system are dangerous — a failed migration should be fixed forward
with a new migration, not rolled back automatically. This eliminates Liquibase's
primary advantage.

The lower operational complexity of Flyway also reduces the risk of
misconfiguration in a solo-developed project where cognitive overhead matters.

## Consequences
- Migrations are plain SQL files — immediately readable and reviewable
- `ddl-auto: validate` means Hibernate will fail fast on startup if the schema
  doesn't match the entity model, catching drift early
- No rollback support — all fixes go forward via new migrations
- Migration filenames follow strict convention: `V{N}__{snake_case_description}.sql`
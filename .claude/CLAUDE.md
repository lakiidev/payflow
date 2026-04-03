# PayFlow

Payment processing backend. University assignment (due mid-May) + portfolio piece targeting distributed systems roles.
Every pattern here solves a real failure mode. The code must be defensible in a senior system design interview.

## Environment
| | |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.x |
| Build | Maven, Jar |
| Kafka | 3.9.0 — KRaft mode, no ZooKeeper |
| Database | PostgreSQL 18 alpine |
| Cache | Redis 8 alpine |
| Schema | Flyway migrations only |
| Tests | Testcontainers — real infra, never H2 or embedded Kafka |

## Package Structure
```
com.payflow
├── application/
│   ├── command/          ← POJOs, zero Spring imports
│   ├── commandhandler/   ← write side, @Transactional(SERIALIZABLE)
│   └── query/            ← read side, @Transactional(readOnly=true)
├── domain/
│   ├── model/            ← aggregates, value objects — NO framework deps, ever
│   ├── event/            ← domain event POJOs
│   └── repository/       ← interfaces only
├── infrastructure/
│   ├── persistence/      ← JPA entities, Spring Data impls
│   ├── kafka/            ← OutboxRelay, consumers, KafkaConfig
│   ├── security/         ← JwtFilter, SecurityConfig
│   └── projection/       ← read model tables + updaters
└── api/
    ├── controller/       ← thin, delegates only — no business logic
    ├── dto/              ← request/response DTOs
    └── exception/        ← @ControllerAdvice
```

## Transaction Rules (quick ref)
| Context | Config |
|---|---|
| Command handlers | `@Transactional(isolation = Isolation.SERIALIZABLE)` |
| Query handlers | `@Transactional(readOnly = true)` |
| Outbox relay | `@Transactional(propagation = Propagation.REQUIRES_NEW)` |
| Consumer idempotency | `TransactionTemplate` wrapping check + process + ack |
| Controllers | never `@Transactional` |

Self-invocation bypasses the AOP proxy — `@Transactional` silently does nothing when called via `this`.

## Data Rules (quick ref)
- Money: `BIGINT` cents — never `DECIMAL`, never `FLOAT`
- PKs: `UUID` generated at domain layer, not by the DB
- Balance: always derived (`SUM(ledger_entries)`), never a mutable column
- Schema: Flyway only — `ddl-auto: validate` everywhere

## Hard Rules (never do these)
- `@Transactional` on a controller
- Spring/JPA imports in `domain/`
- Direct `kafkaTemplate.send()` inside a command handler — use outbox
- H2 or embedded Kafka in tests
- `DECIMAL`/`FLOAT` for money
- `ddl-auto: create` or `update`
- Business logic in a controller or DTO
- Aggregate state mutated from outside the aggregate root
- Hardcoded constants duplicated across classes
- Abstractions for requirements that don't exist yet

## Reference Docs
Read these when working on anything touching the relevant area:

| Topic | File |
|---|---|
| Outbox, CQRS, Saga, Idempotent Consumer, Event Sourcing, Double-Entry | `.claude/docs/architecture-patterns.md` |
| Repository, Factory, Strategy, Observer | `.claude/docs/design-patterns.md` |
| SOLID, YAGNI, DRY, DDD, Fail Fast | `.claude/docs/principles.md` |

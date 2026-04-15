# ADR-010: Kafka over Alternatives for Async Event Delivery

## Status
Accepted — Week 2

## Context
Service requires async delivery of transaction events to multiple independent
consumers: audit logging, analytics, and notifications. The write path must
not be blocked by consumer processing, and events must not be lost if a
consumer is temporarily unavailable.

The choice of messaging infrastructure determines ordering guarantees, replay
capability, consumer isolation, and operational complexity for the lifetime
of the system.

## Decision
Apache Kafka is used as the event streaming platform. Events are published
via the Outbox pattern and consumed by independent consumer groups.

## Alternatives Considered

**RabbitMQ**
- Mature message broker with push-based delivery
- Simpler operationally than Kafka for small workloads
- Messages are deleted after consumption — no replay capability
- No consumer group isolation — competing consumers share a queue by default
- Wrong fit for service: audit and analytics must independently consume
  the same event without affecting each other
- Would require separate queues per consumer, duplicating every event at
  publish time — publisher responsibility grows with each new consumer

**Redis Streams**
- Lightweight streaming primitive built into Redis, already in the stack
- Consumer groups supported — isolation between audit and analytics is possible
- No long-term retention — Redis is an in-memory store, data loss on restart
  without persistence configuration
- Not designed for high-throughput event replay or long retention windows
- Acceptable for ephemeral notifications, not acceptable for financial audit trails

**Polling-based (database outbox read by consumers)**
- No external dependency — consumers poll the outbox table directly
- Simple to reason about and debug
- Does not scale — polling frequency is a tradeoff between latency and DB load
- Every consumer adds a polling load to the primary database
- Violates the read/write split service is building toward in Week 3

**Apache Kafka (chosen)**
- Consumer group semantics provide independent offset tracking per consumer —
  audit, analytics, and notifications each process events at their own pace
  without affecting each other
- Events are retained on disk — replay is possible if a consumer fails or
  a new consumer needs historical data
- Publisher writes once to one topic — adding a new consumer requires no
  changes to the publisher
- Partitioning provides ordered delivery within a partition, enabling
  per-wallet event ordering when needed
- Industry standard for financial event streaming at scale

## Rationale
The core requirement is fan-out to independent consumers with no event loss.
RabbitMQ requires duplicate publishing per consumer. Redis Streams has no
durable retention suitable for financial audit. Polling degrades the primary
database.

Kafka's consumer group model solves fan-out correctly — one event, multiple
independent consumers, each with its own offset. Retention guarantees that
a crashed consumer resumes from where it stopped rather than losing events.
These properties are non-negotiable for a financial audit trail.

The operational overhead of Kafka over RabbitMQ is real but justified by
the replay and fan-out requirements. For local development and the portfolio
deployment, Confluent Cloud free tier eliminates infrastructure management.

## Consequences
- `TransactionOutboxWriter` publishes to a single `transactions` topic —
  adding new consumers requires no publisher changes
- Each consumer registers a unique `groupId` — offset tracking is fully
  independent per consumer
- Kafka becomes a required infrastructure dependency alongside PostgreSQL
- Local development requires a Kafka container in `docker-compose.yml`
- Production uses Confluent Cloud free tier via `KAFKA_BROKERS` env var
- If PayFlow ever moves to a multi-service architecture, Kafka scales
  horizontally without replacing the messaging infrastructure
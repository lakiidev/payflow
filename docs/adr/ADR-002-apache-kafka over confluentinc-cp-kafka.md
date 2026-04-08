# ADR-002: apache/kafka over confluentinc/cp-kafka as the Kafka Docker image

## Status
Accepted — Week 1 

## Context
PayFlow requires a Kafka broker running in Docker Compose for local development
and CI. The two most common images are the official Apache Kafka image
(`apache/kafka`) and Confluent's distribution (`confluentinc/cp-kafka`).

Both support KRaft mode (no ZooKeeper). The choice affects licensing,
configuration complexity, and how closely the local environment mirrors
a potential production deployment.

## Decision
Use `apache/kafka` with KRaft mode. No ZooKeeper. No Confluent Platform
dependency.

## Alternatives Considered

**confluentinc/cp-kafka**
- Confluent's distribution, widely used in production
- Bundles Confluent Platform tooling (Schema Registry, Control Center, etc.)
- Well-documented KRaft setup
- Confluent Community License restricts use in competing SaaS products
- Heavier image — includes tooling PayFlow doesn't need

**apache/kafka**
- Official ASF image under Apache 2.0 license
- No license restrictions
- Minimal image — only what Kafka needs
- KRaft supported natively since Kafka 3.3+

## Rationale
PayFlow has no dependency on Confluent Platform tooling. Schema Registry,
Control Center, and ksqlDB are out of scope for this project. Pulling in
`confluentinc/cp-kafka` would introduce a heavier image and a more restrictive
license for no functional benefit.

`apache/kafka` under Apache 2.0 is the correct choice for a project that only
needs a Kafka broker. It keeps the Docker Compose stack lean and avoids any
licensing ambiguity.

## Consequences
- Docker Compose stack stays minimal — no Confluent Platform overhead
- Apache 2.0 license — no restrictions on use or distribution
- KRaft configuration must be done manually without Confluent's convenience
  wrappers, but this is well-documented for `apache/kafka` as of 3.9.0
- No access to Confluent tooling locally — acceptable since PayFlow doesn't
  use Schema Registry or Control Center
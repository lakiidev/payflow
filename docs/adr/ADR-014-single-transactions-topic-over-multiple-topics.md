# ADR-014: Single transactions Topic over Multiple Topics

## Status
Accepted — Week 2

## Context
PayFlow has three independent consumers of transaction events: audit logging,
analytics aggregation, and notifications. The implementation guide suggests
three separate Kafka topics: `transactions`, `analytics`, and `notifications`.

The question is whether fan-out to multiple consumers is better served by
multiple topics or by a single topic with multiple consumer groups.

## Decision
A single `transactions` topic is used. All consumers — `AuditConsumer`,
`AnalyticsConsumer`, and `NotificationConsumer` — read from the same topic
using independent consumer groups (`audit-group`, `analytics-group`,
`notification-group`). The `OutboxRelay` publishes once per event.

## Alternatives Considered

**Three separate topics (transactions, analytics, notifications)**
- Each consumer reads from its own dedicated topic
- `OutboxRelay` must publish to all three topics per event — three Kafka
  writes per transaction instead of one
- Partial failure: if the relay publishes to `transactions` and `analytics`
  but Kafka is unavailable for `notifications`, the notification event is
  lost while the others are delivered — inconsistent state across topics
- Adding a new consumer requires a new topic and a publisher change —
  publisher and consumer are tightly coupled
- No operational benefit over consumer groups for consumers reading the
  same event type

**Single topic with multiple consumer groups (chosen)**
- `OutboxRelay` publishes once — one Kafka write per transaction
- Each consumer group maintains its own offset independently — `audit-group`
  falling behind does not affect `analytics-group`
- A crashed consumer resumes from its last committed offset without
  affecting other consumer groups
- Adding a new consumer requires no publisher changes — register a new
  consumer group and deploy
- All consumers process the same `TransactionCreated` event — one topic
  correctly models one event type

## Rationale
Topics in Kafka represent event types, not consumers. `TransactionCreated`
is one event type regardless of how many consumers care about it. Splitting
it across three topics conflates the event with its consumers — a design
that couples publisher to consumer count.

Consumer groups exist precisely to solve fan-out: multiple independent
consumers reading the same event stream at their own pace with isolated
offset tracking. Using multiple topics to achieve the same isolation adds
publisher complexity and introduces partial failure modes with no benefit.

The three-topic approach would be appropriate if the topics carried different
event types with different schemas, different retention requirements, or
different access controls. None of those conditions apply in PayFlow — all
three consumers read `TransactionCreated` with the same payload, the same
retention window, and no access control differentiation.

## Scaling Path
The single topic design scales cleanly as PayFlow grows:

**New event types** — `WalletFrozen`, `TransactionFailed`, `UserSuspended`
would each warrant their own topic because they are semantically distinct
events with different schemas. The single `transactions` topic would remain
for `TransactionCreated`. Topic proliferation is driven by event type
diversity, not consumer count.

**New consumers** — a fraud detection service or reconciliation service
consuming `TransactionCreated` registers a new consumer group against the
existing `transactions` topic. Zero publisher changes required.

**True service boundaries** — if audit, analytics, and notifications become
separate deployable services owned by separate teams, each service still
reads from the same `transactions` topic with its own consumer group.
Topics are not split per service — they are split per event type.

**Increased throughput** — partition count on the `transactions` topic can
be increased independently of consumer group configuration. Each consumer
group scales its partition assignment independently.

## Consequences
- `OutboxRelay` publishes to `transactions` topic only — one Kafka write
  per committed transaction
- `AuditConsumer`, `AnalyticsConsumer`, `NotificationConsumer` each declare
  a unique `groupId` — offset tracking is fully independent
- No `analytics` or `notifications` Kafka topics are created
- In Week 3 when `AnalyticsConsumer` is introduced, no topic infrastructure
  changes are required — it registers against the existing `transactions` topic
- If a semantically distinct event type emerges (e.g. `WalletFrozen`),
  a new topic is introduced at that point — not preemptively
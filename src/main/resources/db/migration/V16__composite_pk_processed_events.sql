-- Change PK from (event_id) to (event_id, consumer_group) to support per-consumer-group idempotency
ALTER TABLE processed_events DROP CONSTRAINT processed_events_pkey;
ALTER TABLE processed_events ADD CONSTRAINT processed_events_pkey PRIMARY KEY (event_id, consumer_group);

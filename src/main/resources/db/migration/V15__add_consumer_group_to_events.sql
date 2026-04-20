ALTER TABLE processed_events ADD COLUMN consumer_group VARCHAR(50);

UPDATE processed_events SET consumer_group = 'audit';

ALTER TABLE processed_events ALTER COLUMN consumer_group SET NOT NULL;

ALTER TABLE processed_events ADD CONSTRAINT processed_events_event_id_consumer_group_key
    UNIQUE (event_id, consumer_group);

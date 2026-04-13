-- Fix processed_events PK: VARCHAR(255) → UUID to match ProcessedEvent entity
ALTER TABLE processed_events
    ALTER COLUMN event_id TYPE UUID USING event_id::UUID;

-- Fix audit_logs.created_at: TIMESTAMP → TIMESTAMPTZ (missed in V6 fix_timestamps)
ALTER TABLE audit_logs
    ALTER COLUMN created_at TYPE TIMESTAMPTZ;

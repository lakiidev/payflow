CREATE TABLE reconciliation_alerts
(
    id          UUID        DEFAULT gen_random_uuid() NOT NULL PRIMARY KEY,
    type        VARCHAR(40)                           NOT NULL,
    detail      TEXT                                  NOT NULL,
    occurred_at TIMESTAMPTZ DEFAULT NOW()             NOT NULL
);
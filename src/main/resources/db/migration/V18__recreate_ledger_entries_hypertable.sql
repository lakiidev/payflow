DROP TABLE public.ledger_entries;

CREATE TABLE ledger_entries
(
    id             UUID             DEFAULT gen_random_uuid() NOT NULL,
    transaction_id UUID             NOT NULL REFERENCES transactions (id),
    wallet_id      UUID             NOT NULL REFERENCES wallets (id),
    entry_type     VARCHAR(10)      NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount         BIGINT           NOT NULL CHECK (amount > 0),
    balance_after  BIGINT           NOT NULL,
    created_at     TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, created_at)
);

CREATE INDEX idx_ledger_transaction ON ledger_entries (transaction_id);
CREATE INDEX idx_ledger_wallet ON ledger_entries (wallet_id, created_at DESC);

SELECT create_hypertable('ledger_entries', 'created_at', chunk_time_interval => INTERVAL '1 month');
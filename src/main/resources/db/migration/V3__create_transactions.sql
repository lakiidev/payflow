CREATE TABLE transactions
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    type            VARCHAR(50)         NOT NULL,
    from_wallet_id  UUID REFERENCES wallets (id),
    to_wallet_id    UUID REFERENCES wallets (id),
    amount          DECIMAL(19, 4)      NOT NULL,
    currency        VARCHAR(3)          NOT NULL,
    status          VARCHAR(50)         NOT NULL,
    description     TEXT,
    metadata        JSONB,
    created_at      TIMESTAMP        DEFAULT NOW(),
    completed_at    TIMESTAMP,
    CHECK (amount > 0),
    CHECK (from_wallet_id IS NOT NULL OR to_wallet_id IS NOT NULL)
);

CREATE INDEX idx_tx_from_wallet ON transactions (from_wallet_id);
CREATE INDEX idx_tx_to_wallet ON transactions (to_wallet_id);
CREATE INDEX idx_tx_status ON transactions (status);
CREATE INDEX idx_tx_created_at ON transactions (created_at DESC);
CREATE INDEX idx_tx_idempotency ON transactions (idempotency_key);
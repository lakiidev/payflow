CREATE TABLE wallets
(
    id              UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    user_id         UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    currency        VARCHAR(3)     NOT NULL,
    current_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.00,
    version         BIGINT         NOT NULL DEFAULT 0,
    status          VARCHAR(50)             DEFAULT 'ACTIVE',
    created_at      TIMESTAMP               DEFAULT NOW(),
    updated_at      TIMESTAMP               DEFAULT NOW(),
    UNIQUE (user_id, currency)
);

CREATE INDEX idx_wallets_user_id ON wallets (user_id);
CREATE INDEX idx_wallets_currency ON wallets (currency);
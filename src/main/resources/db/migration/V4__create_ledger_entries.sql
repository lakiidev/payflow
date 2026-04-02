CREATE TABLE ledger_entries
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID           NOT NULL REFERENCES transactions (id),
    wallet_id      UUID           NOT NULL REFERENCES wallets (id),
    entry_type     VARCHAR(10)    NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount         DECIMAL(19, 4) NOT NULL CHECK (amount > 0),
    balance_after  DECIMAL(19, 4) NOT NULL,
    created_at     TIMESTAMP        DEFAULT NOW()
);

CREATE INDEX idx_ledger_transaction ON ledger_entries (transaction_id);
CREATE INDEX idx_ledger_wallet ON ledger_entries (wallet_id, created_at DESC);
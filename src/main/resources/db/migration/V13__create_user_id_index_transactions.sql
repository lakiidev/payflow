
CREATE INDEX idx_transactions_user_id_created_at
    ON transactions (user_id, created_at DESC);
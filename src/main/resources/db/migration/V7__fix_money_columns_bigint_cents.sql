ALTER TABLE wallets
ALTER COLUMN current_balance TYPE BIGINT
USING (current_balance*100)::BIGINT;

ALTER TABLE transactions
ALTER COLUMN amount TYPE BIGINT
USING (amount*100)::BIGINT;

ALTER TABLE ledger_entries
ALTER COLUMN amount TYPE BIGINT
USING (amount*100)::BIGINT;

ALTER TABLE ledger_entries
ALTER COLUMN balance_after TYPE BIGINT
USING (balance_after*100)::BIGINT;


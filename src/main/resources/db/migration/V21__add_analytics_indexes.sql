CREATE INDEX IF NOT EXISTS idx_ledger_entries_transaction_entry_type
    ON ledger_entries (transaction_id, entry_type);
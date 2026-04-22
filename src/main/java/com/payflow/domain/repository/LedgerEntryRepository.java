package com.payflow.domain.repository;

import com.payflow.domain.model.ledger.LedgerEntry;

public interface LedgerEntryRepository {
    LedgerEntry save(LedgerEntry ledgerEntry);
    void deleteAll();
}
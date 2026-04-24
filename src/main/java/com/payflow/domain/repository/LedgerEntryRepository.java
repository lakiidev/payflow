package com.payflow.domain.repository;

import com.payflow.domain.model.ledger.LedgerEntry;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository {
    LedgerEntry save(LedgerEntry ledgerEntry);
    List<LedgerEntry> findAllByTransactionId(UUID walletId);
    void deleteAll();
}
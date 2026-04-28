package com.payflow.domain.repository;

import com.payflow.api.dto.response.BalanceHistoryResponse;
import com.payflow.api.dto.response.MonthlySummaryResponse;
import com.payflow.api.dto.response.SpendingByCategoryResponse;
import com.payflow.domain.model.ledger.LedgerEntry;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository {
    LedgerEntry save(LedgerEntry ledgerEntry);
    List<LedgerEntry> findAllByTransactionId(UUID transactionId);
    void deleteAll();
    List<BalanceHistoryResponse> findBalanceHistory(UUID walletId, Instant from, Instant to, String interval);
    List<SpendingByCategoryResponse> findSpendingByCategory(UUID walletId, Instant from, Instant to);
    MonthlySummaryResponse findMonthlySummary(UUID walletId, Instant from, Instant to);
}
package com.payflow.application.query;

import com.payflow.api.dto.response.BalanceHistoryResponse;
import com.payflow.api.dto.response.MonthlySummaryResponse;
import com.payflow.api.dto.response.SpendingByCategoryResponse;
import com.payflow.domain.model.wallet.WalletNotFoundException;
import com.payflow.domain.repository.LedgerEntryRepository;
import com.payflow.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class AnalyticsQueryHandler {
    private final LedgerEntryRepository ledgerEntryRepository;
    private final WalletRepository walletRepository;

    public record BalanceHistoryQuery(
            UUID walletId,
            UUID requestingUserId,
            LocalDate from,
            LocalDate to,
            String interval          // TimescaleDB interval string, e.g. "1 day", "1 hour"
    ) {}

    public record SpendingByCategoryQuery(
            UUID walletId,
            UUID requestingUserId,
            LocalDate from,
            LocalDate to
    ) {}

    public record MonthlySummaryQuery(
            UUID walletId,
            UUID requestingUserId,
            YearMonth month
    ) {}

    @Transactional(readOnly = true)
    public List<BalanceHistoryResponse> balanceHistory(BalanceHistoryQuery query) {
        assertOwnership(query.walletId(), query.requestingUserId());
        return ledgerEntryRepository.findBalanceHistory(
                query.walletId(),
                query.from().atStartOfDay(ZoneOffset.UTC).toInstant(),
                query.to().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                query.interval()
        );
    }

    @Transactional(readOnly = true)
    public List<SpendingByCategoryResponse> spendingByCategory(SpendingByCategoryQuery query) {
        assertOwnership(query.walletId(), query.requestingUserId());
        return ledgerEntryRepository.findSpendingByCategory(
                query.walletId(),
                query.from().atStartOfDay(ZoneOffset.UTC).toInstant(),
                query.to().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        );
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse monthlySummary(MonthlySummaryQuery query) {
        assertOwnership(query.walletId(), query.requestingUserId());
        LocalDate from = query.month().atDay(1);
        LocalDate to = query.month().atEndOfMonth();
        return ledgerEntryRepository
                .findMonthlySummary
                        (query.walletId(), from.atStartOfDay(ZoneOffset.UTC).toInstant(),
                                to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                        );
    }

    private void assertOwnership(UUID walletId, UUID requestingUserId) {
        var wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        if (!wallet.getUserId().equals(requestingUserId)) {
            throw new WalletNotFoundException(walletId);
        }
    }
}

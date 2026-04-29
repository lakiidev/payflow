package com.payflow.api.controller;

import com.payflow.api.dto.response.BalanceHistoryResponse;
import com.payflow.api.dto.response.MonthlySummaryResponse;
import com.payflow.api.dto.response.SpendingByCategoryResponse;
import com.payflow.application.query.AnalyticsQueryHandler;
import com.payflow.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryHandler analyticsQueryHandler;

    // GET /analytics/balance-history?walletId=&from=&to=&bucket=1 day
    @GetMapping("/balance-history")
    public List<BalanceHistoryResponse> balanceHistory(
            @RequestParam UUID walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "1 day") String interval,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        var query = new AnalyticsQueryHandler.BalanceHistoryQuery(
                walletId,
                UUID.fromString(userDetails.getUsername()),
                from,
                to,
                interval
        );
        return analyticsQueryHandler.balanceHistory(query);
    }

    // GET /analytics/spending-by-category?walletId=&from=&to=
    @GetMapping("/spending-by-category")
    public List<SpendingByCategoryResponse> spendingByCategory(
            @RequestParam UUID walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal User user
    ) {
        var query = new AnalyticsQueryHandler.SpendingByCategoryQuery(
                walletId,
                user.getId(),
                from,
                to
        );
        return analyticsQueryHandler.spendingByCategory(query);
    }

    // GET /analytics/monthly-summary?walletId=&month=2025-04
    @GetMapping("/monthly-summary")
    public MonthlySummaryResponse monthlySummary(
            @RequestParam UUID walletId,
            @RequestParam String month,   // e.g. "2025-04"
            @AuthenticationPrincipal User user
    ) {
        var query = new AnalyticsQueryHandler.MonthlySummaryQuery(
                walletId,
                user.getId(),
                YearMonth.parse(month)
        );
        return analyticsQueryHandler.monthlySummary(query);
    }
}
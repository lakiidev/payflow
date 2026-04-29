package com.payflow.api.controller;

import com.payflow.api.dto.response.BalanceHistoryResponse;
import com.payflow.api.dto.response.MonthlySummaryResponse;
import com.payflow.api.dto.response.SpendingByCategoryResponse;
import com.payflow.application.dto.TransactionView;
import com.payflow.application.port.CsvExportPort;
import com.payflow.application.port.PdfExportPort;
import com.payflow.application.query.AnalyticsQueryHandler;
import com.payflow.application.query.WalletStatementQueryHandler;
import com.payflow.domain.model.user.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryHandler analyticsQueryHandler;
    private final WalletStatementQueryHandler walletStatementQueryHandler;
    private final CsvExportPort csvExportPort;
    private final PdfExportPort pdfExportPort;

    // GET /analytics/balance-history?walletId=&from=&to=&bucket=1 day
    @GetMapping("api/v1/balance-history")
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

    @GetMapping(value = "/{walletId}/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> export(@PathVariable UUID walletId,
                       @RequestParam Instant from,
                       @RequestParam Instant to,
                       @RequestParam String format,
                       @AuthenticationPrincipal User user,
                       HttpServletResponse response)
    {
        List<TransactionView> transactions = walletStatementQueryHandler.handle(
                new WalletStatementQueryHandler.Query(user.getId(), walletId, from, to)
        );

        ExportFormat exportFormat = ExportFormat.fromString(format);

        if (exportFormat == ExportFormat.PDF) {
            StreamingResponseBody stream = out -> pdfExportPort.writePdf(walletId, transactions, out);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"statement-%s.pdf\"".formatted(walletId))
                    .body(stream);
        } else {
            StreamingResponseBody stream = out -> csvExportPort.writeCsv(transactions, out);
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"statement-%s.csv\"".formatted(walletId))
                    .body(stream);
        }
    }
}
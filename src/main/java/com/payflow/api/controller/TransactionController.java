package com.payflow.api.controller;

import com.payflow.api.dto.request.TransactionRequest;
import com.payflow.api.dto.response.TransactionResponse;
import com.payflow.application.command.transactions.DepositCommandHandler;
import com.payflow.application.command.transactions.TransferCommandHandler;
import com.payflow.application.command.transactions.WithdrawCommandHandler;
import com.payflow.application.dto.TransactionView;
import com.payflow.application.port.CsvExportPort;
import com.payflow.application.port.PdfExportPort;
import com.payflow.application.query.TransactionQueryHandler;
import com.payflow.application.query.WalletStatementQueryHandler;
import com.payflow.domain.model.user.User;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/transactions")
public class TransactionController {
    private final TransactionQueryHandler transactionQueryHandler;
    private final DepositCommandHandler depositCommandHandler;
    private final WithdrawCommandHandler withdrawCommandHandler;
    private final TransferCommandHandler transferCommandHandler;
    private final WalletStatementQueryHandler walletStatementQueryHandler;
    private final CsvExportPort  csvExportPort;
    private final PdfExportPort pdfExportPort;

    @GetMapping
    public Page<TransactionResponse> getTransactions(Pageable pageable,
                                                     @AuthenticationPrincipal User user) {
        return transactionQueryHandler
                .handle(new TransactionQueryHandler
                        .GetTransactionsQuery(user.getId(), pageable));
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransaction(@PathVariable UUID id, @AuthenticationPrincipal User user)
    {
        return transactionQueryHandler.handle(new TransactionQueryHandler.GetTransactionQuery(id, user.getId()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @RequestBody @Valid TransactionRequest.DepositRequest request,
            @AuthenticationPrincipal User user)
    {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(depositCommandHandler.handle(
                        new DepositCommandHandler.Command(
                                request.idempotencyKey().toString(),
                                request.toWalletId(),
                                user.getId(),
                                request.amount()
                        )
                        )
                ));
    }
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @RequestBody @Valid TransactionRequest.WithdrawRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(withdrawCommandHandler.handle(
                        new WithdrawCommandHandler.Command(
                                request.idempotencyKey().toString(),
                                request.fromWalletId(),
                                user.getId(),
                                request.amount()
                        ))));
    }
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @RequestBody @Valid TransactionRequest.TransferRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(transferCommandHandler.handle(
                        new TransferCommandHandler.Command(
                                request.idempotencyKey().toString(),
                                request.fromWalletId(),
                                request.toWalletId(),
                                user.getId(),
                                request.amount()
                        ))));
    }

    @GetMapping(value = "/{walletId}/transactions/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void export(@PathVariable UUID walletId,
                       @RequestParam(required = false) Instant from,
                       @RequestParam(required = false) Instant to,
                       @RequestParam ExportFormat format,
                       @AuthenticationPrincipal User user,
                       HttpServletResponse response) throws IOException
    {
        Instant effectiveFrom = from != null ? from : Instant.EPOCH;
        Instant effectiveTo = to != null ? to : Instant.now();

        List<TransactionView> transactions = walletStatementQueryHandler.handle(
                new WalletStatementQueryHandler.Query(user.getId(), walletId, effectiveFrom, effectiveTo)
        );

            if(ExportFormat.PDF.equals(format)) {
                response.setContentType(MediaType.APPLICATION_PDF_VALUE);
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"statement-%s.pdf\"".formatted(walletId));
                response.getOutputStream().write(pdfExportPort.generatePdf(walletId, transactions));
            }
            else if(ExportFormat.CSV.equals(format)) {
                response.setContentType("text/csv");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"statement-%s.csv\"".formatted(walletId));
                csvExportPort.writeCsv(transactions, response.getOutputStream());
            }
            else {
                throw new UnsupportedOperationException("Unsupported export format: " + format);
            }
    }
}

package com.payflow.infrastructure.io;

import com.payflow.application.dto.TransactionView;
import com.payflow.domain.model.ledger.EntryType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PdfStatementAdapterTest {

    private final PdfStatementAdapter adapter = new PdfStatementAdapter();

    @Test
    void generatePdf_shouldReturnValidPdf_whenTransactionsPresent() {
        // Given
        UUID walletId = UUID.randomUUID();
        List<TransactionView> transactions = List.of(
                new TransactionView(UUID.randomUUID(), Instant.parse("2026-01-01T00:00:00Z"),
                        EntryType.CREDIT, 5000L, 5000L)
        );

        assertPdfWithTransactions(walletId, transactions);
    }

    @Test
    void generatePdf_shouldReturnValidPdf_whenNoTransactions() {
        // Given
        UUID walletId = UUID.randomUUID();
        List<TransactionView> transactions = List.of();

        assertPdfWithTransactions(walletId,transactions);
    }

    private void assertPdfWithTransactions(UUID walletId,List<TransactionView> transactions){
        // When
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        adapter.writePdf(walletId, transactions.stream(), out);

        // Then
        byte[] pdf = out.toByteArray();
        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, StandardCharsets.ISO_8859_1)).startsWith("%PDF");
    }
}
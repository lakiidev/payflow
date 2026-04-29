package com.payflow.infrastructure.io;

import com.payflow.application.dto.TransactionView;
import com.payflow.domain.model.ledger.EntryType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CsvStatementAdapterTest {

    private final CsvStatementAdapter adapter = new CsvStatementAdapter();

    @Test
    void shouldWriteHeaderAndRowWhenTransactionsPresent() throws IOException {
        // Given
        List<TransactionView> transactions = List.of(
                new TransactionView(UUID.randomUUID(), Instant.parse("2026-01-01T00:00:00Z"),
                        EntryType.DEBIT, 10050L, 89950L)
        );
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // When
        adapter.writeCsv(transactions, out);

        // Then
        String csv = out.toString(StandardCharsets.UTF_8);
        assertThat(csv).contains("transaction_id,entry_type,amount,balance_after,created_at")
                .contains("DEBIT")
                .contains("100.50")
                .contains("899.50");
    }

    @Test
    void shouldWriteHeaderOnlyWhenNoTransactions() throws IOException {
        // Given
        List<TransactionView> transactions = List.of();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // When
        adapter.writeCsv(transactions, out);

        // Then
        String csv = out.toString(StandardCharsets.UTF_8);
        assertThat(csv).contains("transaction_id,entry_type,amount,balance_after,created_at");
        assertThat(csv.lines().count()).isEqualTo(1);
    }
}
package com.payflow.domain.model.transaction;

import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    private static final UUID USER_ID      = UUID.randomUUID();
    private static final UUID WALLET_ID    = UUID.randomUUID();
    private static final Currency EUR      = Currency.getInstance("EUR");

    @Test
    void shouldCreateTransactionWithPendingStatus() {
        // When
        Transaction tx = Transaction.create("idem-key-1", TransactionType.DEPOSIT,
                null, WALLET_ID, 5000L, EUR, USER_ID);

        // Then
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(tx.getIdempotencyKey()).isEqualTo("idem-key-1");
        assertThat(tx.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(tx.getFromWalletId()).isNull();
        assertThat(tx.getToWalletId()).isEqualTo(WALLET_ID);
        assertThat(tx.getAmount()).isEqualTo(5000L);
        assertThat(tx.getCurrency()).isEqualTo(EUR);
        assertThat(tx.getUserId()).isEqualTo(USER_ID);
        assertThat(tx.getCompletedAt()).isNull();
    }

    @Test
    void shouldSetSuccessStatusAndCompletedAtOnComplete() {
        // Given
        Transaction tx = Transaction.create("idem-key-1", TransactionType.DEPOSIT,
                null, WALLET_ID, 5000L, EUR, USER_ID);

        // When
        tx.complete();

        // Then
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(tx.getCompletedAt()).isNotNull();
    }
}

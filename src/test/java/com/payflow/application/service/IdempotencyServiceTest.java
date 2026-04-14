package com.payflow.application.service;

import com.payflow.domain.model.transaction.Transaction;
import com.payflow.domain.model.transaction.TransactionType;
import com.payflow.infrastructure.persistence.jpa.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock private TransactionRepository transactionRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    private static final String IDEM_KEY = "idem-key-1";
    private static final UUID   WALLET_ID = UUID.randomUUID();
    private static final UUID   USER_ID   = UUID.randomUUID();
    private static final Currency EUR     = Currency.getInstance("EUR");

    private Transaction pendingDeposit() {
        return Transaction.create(IDEM_KEY, TransactionType.DEPOSIT,
                null, WALLET_ID, 5000L, EUR, USER_ID);
    }


    @Test
    void shouldReturnExistingTransactionWhenKeyMatches() {
        // Given
        Transaction existing = pendingDeposit();
        when(transactionRepository.findByIdempotencyKey(IDEM_KEY))
                .thenReturn(Optional.of(existing));

        // When
        Optional<Transaction> result = idempotencyService.findDuplicate(IDEM_KEY);

        // Then
        assertThat(result).contains(existing);
    }

    @Test
    void shouldReturnEmptyWhenNoExistingTransactionFound() {
        // Given
        when(transactionRepository.findByIdempotencyKey(IDEM_KEY))
                .thenReturn(Optional.empty());

        // When
        Optional<Transaction> result = idempotencyService.findDuplicate(IDEM_KEY);

        // Then
        assertThat(result).isEmpty();
    }


    @Test
    void shouldSaveAndReturnTransactionOnFirstInsert() {
        // Given
        Transaction tx = pendingDeposit();
        when(transactionRepository.save(tx)).thenReturn(tx);

        // When
        Transaction result = idempotencyService.deduplicateOrSave(tx);

        // Then
        assertThat(result).isSameAs(tx);
    }

    @Test
    void shouldReturnWinnerWhenConcurrentInsertViolatesConstraint() {
        // Given — two requests race; this thread loses the unique constraint
        Transaction tx     = pendingDeposit();
        Transaction winner = pendingDeposit();
        when(transactionRepository.save(tx))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));
        when(transactionRepository.findByIdempotencyKey(IDEM_KEY))
                .thenReturn(Optional.of(winner));

        // When
        Transaction result = idempotencyService.deduplicateOrSave(tx);

        // Then
        assertThat(result).isSameAs(winner);
    }

    @Test
    void shouldThrowWhenConstraintFiredButRecordStillNotFound() {
        // Given — constraint violation with no record to re-fetch indicates data corruption
        Transaction tx = pendingDeposit();
        when(transactionRepository.save(tx))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));
        when(transactionRepository.findByIdempotencyKey(IDEM_KEY))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> idempotencyService.deduplicateOrSave(tx))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Idempotency constraint violated but record not found");
    }
}

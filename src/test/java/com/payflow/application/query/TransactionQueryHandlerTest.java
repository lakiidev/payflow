package com.payflow.application.query;

import com.payflow.domain.model.transaction.TransactionNotFoundException;
import com.payflow.domain.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionQueryHandlerTest {

    @Mock private TransactionRepository repository;

    @InjectMocks
    private TransactionQueryHandler handler;

    @Test
    void shouldThrowWhenTransactionNotFound() {
        // Given
        UUID transactionId  = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.empty());

        // When / Then
        var query = new TransactionQueryHandler.GetTransactionQuery(transactionId, userId);

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(TransactionNotFoundException.class);
    }
}

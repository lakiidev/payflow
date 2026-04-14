package com.payflow.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payflow.domain.model.outbox.OutboxEvent;
import com.payflow.domain.model.outbox.OutboxEventStatus;
import com.payflow.domain.model.transaction.Transaction;
import com.payflow.domain.model.transaction.TransactionType;
import com.payflow.infrastructure.persistence.jpa.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionOutboxWriterTest {

    @Mock private OutboxRepository outboxRepository;

    private TransactionOutboxWriter writer;

    private static final UUID USER_ID   = UUID.randomUUID();
    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final Currency EUR   = Currency.getInstance("EUR");

    @BeforeEach
    void setUp() {
        // real ObjectMapper so serialization is actually exercised
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        writer = new TransactionOutboxWriter(outboxRepository, objectMapper);
    }

    @Test
    void shouldSaveOutboxEventWithCorrectFields() {
        // Given
        Transaction tx = Transaction.create("idem-key-1", TransactionType.DEPOSIT,
                null, WALLET_ID, 5000L, EUR, USER_ID);
        tx.complete();

        // When
        writer.publishTransactionCreated(tx);

        // Then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(captor.capture());
        OutboxEvent event = captor.getValue();

        assertThat(event.getAggregateId()).isEqualTo(tx.getId());
        assertThat(event.getAggregateType()).isEqualTo("Transaction");
        assertThat(event.getEventType()).isEqualTo("TransactionCreated");
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(event.getPayload()).isNotBlank();
    }

    @Test
    void shouldSerializePayloadAsValidJson() throws Exception {
        // Given
        Transaction tx = Transaction.create("idem-key-1", TransactionType.DEPOSIT,
                null, WALLET_ID, 5000L, EUR, USER_ID);
        tx.complete();

        // When
        writer.publishTransactionCreated(tx);

        // Then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(captor.capture());

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        TransactionOutboxWriter.TransactionCreatedPayload payload = mapper.readValue(
                captor.getValue().getPayload(),
                TransactionOutboxWriter.TransactionCreatedPayload.class
        );
        assertThat(payload.transactionId()).isEqualTo(tx.getId());
        assertThat(payload.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(payload.amountCents()).isEqualTo(5000L);
        assertThat(payload.currency()).isEqualTo(EUR);
    }

    @Test
    void shouldThrowWhenSerializationFails() {
        // Given — ObjectMapper that always fails
        ObjectMapper broken = new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) throws com.fasterxml.jackson.core.JsonProcessingException {
                throw new JsonGenerationException("forced failure", (JsonGenerator) null);
            }
        };
        TransactionOutboxWriter brokenWriter = new TransactionOutboxWriter(outboxRepository, broken);
        Transaction tx = Transaction.create("idem-key-1", TransactionType.DEPOSIT,
                null, WALLET_ID, 5000L, EUR, USER_ID);

        // When / Then
        assertThatThrownBy(() -> brokenWriter.publishTransactionCreated(tx))
                .isInstanceOf(IllegalStateException.class);
    }
}

package com.payflow.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payflow.domain.model.audit.AuditLog;
import com.payflow.domain.model.event.ProcessedEvent;
import com.payflow.infrastructure.persistence.jpa.AuditLogRepository;
import com.payflow.infrastructure.persistence.jpa.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditConsumerTest {

    @Mock private TransactionTemplate transactionTemplate;
    @Mock private ProcessedEventRepository processedEventRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private Acknowledgment ack;

    private AuditConsumer consumer;

    private static final UUID EVENT_ID  = UUID.randomUUID();
    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final String AUDIT_GROUP = "audit";

    private String validPayload;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        consumer = new AuditConsumer(transactionTemplate, processedEventRepository, auditLogRepository, objectMapper);

        validPayload = """
                {"transactionId":"%s","type":"DEPOSIT","fromWalletId":null,"toWalletId":"%s",\
                "amountCents":5000,"currency":"EUR","completedAt":"2024-01-01T00:00:00Z"}"""
                .formatted(EVENT_ID, WALLET_ID);

        doAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        }).when(transactionTemplate).execute(any());
    }

    @Test
    void shouldSaveAuditLogAndMarkEventProcessedWhenEventIsNew() {
        // Given
        when(processedEventRepository.existsByEventIdAndConsumerGroup(EVENT_ID, AUDIT_GROUP)).thenReturn(false);

        // When
        consumer.handle(validPayload, ack);

        // Then
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        AuditLog saved = auditCaptor.getValue();
        assertThat(saved.getAction()).isEqualTo("DEPOSIT");
        assertThat(saved.getEntityType()).isEqualTo("Transaction");
        assertThat(saved.getEntityId()).isEqualTo(EVENT_ID);

        verify(processedEventRepository).save(any(ProcessedEvent.class));
        verify(ack).acknowledge();
    }

    @Test
    void shouldSkipAuditLogAndAckWhenSameEventAlreadyProcessedByAuditGroup() {
        // Given
        when(processedEventRepository.existsByEventIdAndConsumerGroup(EVENT_ID, AUDIT_GROUP)).thenReturn(true);

        // When
        consumer.handle(validPayload, ack);

        // Then — idempotency: no side effects, but acknowledge the known duplicate
        verify(auditLogRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
        verify(ack).acknowledge();
    }

    @Test
    void shouldProcessEventWhenSameEventIdAlreadyProcessedByDifferentConsumerGroup() {
        // Given — "analytics" group processed this event; processed_events has (EVENT_ID, "analytics")
        // but existsByEventIdAndConsumerGroup(EVENT_ID, "audit") still returns false, so audit proceeds
        when(processedEventRepository.existsByEventIdAndConsumerGroup(EVENT_ID, AUDIT_GROUP)).thenReturn(false);

        // When
        consumer.handle(validPayload, ack);

        // Then — audit group is independent; it writes its own processed_events row and acks
        verify(auditLogRepository).save(any(AuditLog.class));
        verify(processedEventRepository).save(any(ProcessedEvent.class));
        verify(ack).acknowledge();
    }

    @Test
    void shouldThrowIllegalStateWhenPayloadIsInvalidJson() {
        // When / Then
        assertThatThrownBy(() -> consumer.handle("not-valid-json", ack))
                .isInstanceOf(IllegalStateException.class);

        verify(auditLogRepository, never()).save(any());
        verify(ack, never()).acknowledge();
    }

    @Test
    void shouldNotAcknowledgeWhenRepositorySaveThrows() {
        // Given
        when(processedEventRepository.existsByEventIdAndConsumerGroup(EVENT_ID, AUDIT_GROUP)).thenReturn(false);
        when(auditLogRepository.save(any())).thenThrow(new RuntimeException());

        // When / Then — exception propagates out of the transaction callback
        assertThatThrownBy(() -> consumer.handle(validPayload, ack))
                .isInstanceOf(RuntimeException.class);

        verify(processedEventRepository, never()).save(any());
        verify(ack, never()).acknowledge();
    }
}

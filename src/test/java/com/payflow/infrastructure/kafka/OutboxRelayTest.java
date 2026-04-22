package com.payflow.infrastructure.kafka;

import com.payflow.domain.model.outbox.OutboxEvent;
import com.payflow.domain.model.outbox.OutboxEventStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock private OutboxService outboxService;
    @Mock private TransactionEventPublisher publisher;

    @InjectMocks private OutboxRelay relay;

    @Test
    void firstFailureStaysPending() {
        // Given
        ReflectionTestUtils.setField(relay, "batchSize", 10);
        ReflectionTestUtils.setField(relay, "maxRetries", 3);
        OutboxEvent event = pendingEvent(0);
        when(outboxService.fetchPending(10)).thenReturn(List.of(event));
        doThrow(new RuntimeException("broker down")).when(publisher).publish(event);

        // When
        relay.relay();

        // Then
        verify(outboxService).incrementRetry(event.getId(), "broker down");
        verify(outboxService, never()).markAsFailed(event.getId());
        verify(outboxService, never()).markAsProcessed(event.getId());
    }

    @Test
    void nthFailureTransitionsToFailed() {
        // Given
        ReflectionTestUtils.setField(relay, "batchSize", 10);
        ReflectionTestUtils.setField(relay, "maxRetries", 3);
        OutboxEvent event = pendingEvent(2); // retryCount = 2, next attempt is 3 >= maxRetries
        when(outboxService.fetchPending(10)).thenReturn(List.of(event));
        doThrow(new RuntimeException("broker down")).when(publisher).publish(event);

        // When
        relay.relay();

        // Then
        verify(outboxService).incrementRetry(event.getId(), "broker down");
        verify(outboxService).markAsFailed(event.getId());
        verify(outboxService, never()).markAsProcessed(event.getId());
    }

    @Test
    void successfulPublishMarksProcessed() {
        // Given
        ReflectionTestUtils.setField(relay, "batchSize", 10);
        ReflectionTestUtils.setField(relay, "maxRetries", 3);
        OutboxEvent event = pendingEvent(0);
        when(outboxService.fetchPending(10)).thenReturn(List.of(event));

        // When
        relay.relay();

        // Then
        verify(outboxService).markAsProcessed(event.getId());
        verify(outboxService, never()).incrementRetry(any(), any());
        verify(outboxService, never()).markAsFailed(any());
    }

    private OutboxEvent pendingEvent(int retryCount) {
        OutboxEvent event = new OutboxEvent();
        ReflectionTestUtils.setField(event, "id", UUID.randomUUID());
        event.setStatus(OutboxEventStatus.PENDING);
        event.setRetryCount(retryCount);
        return event;
    }
}
package com.payflow.infrastructure.kafka;

import com.payflow.domain.model.outbox.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;




@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxService outboxService;
    private final TransactionEventPublisher publisher;

    @Value("${payflow.outbox.batch-size}")
    private Integer batchSize;

    @Value("${payflow.outbox.max-retries}")
    private Integer maxRetries;

    @Scheduled(fixedDelayString = "${payflow.outbox.poll-interval-ms}")
    public void relay() {
        List<OutboxEvent> events = outboxService.fetchPending(batchSize);

        for (OutboxEvent event : events) {
            try {
                publisher.publish(event);
                outboxService.markAsProcessed(event.getId());
            } catch (Exception e) {
                outboxService.incrementRetry(event.getId(), e.getMessage());
                if (event.getRetryCount() + 1 >= maxRetries) {
                    outboxService.markAsFailed(event.getId());
                }
            }
        }
    }
}

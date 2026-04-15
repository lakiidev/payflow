package com.payflow.infrastructure.kafka;

import com.payflow.domain.model.outbox.OutboxEvent;
import com.payflow.infrastructure.persistence.jpa.OutboxRepository;
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

    @Scheduled(fixedDelayString = "${payflow.outbox.poll-interval-ms}")
    public void relay() {
        List<OutboxEvent> events = outboxService.fetchAndMarkAsProcessing(batchSize);

        for (OutboxEvent event : events) {
            try{
                publisher.publish(event);
                outboxService.markAsProcessed(event.getId());
            }catch (Exception _){
                outboxService.markAsFailed(event.getId());
            }
        }
    }


}

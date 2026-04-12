package com.payflow.infrastructure.kafka;

import com.payflow.domain.model.outbox.OutboxEvent;
import com.payflow.infrastructure.persistence.jpa.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final TransactionEventPublisher publisher; // producer


    @Value("${payflow.outbox.batch-size}")
    private Integer batchSize;

    @Scheduled(fixedDelayString = "${payflow.outbox.poll-interval-ms}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void relay() {
        List<OutboxEvent> pending = outboxRepository
                .findByStatusOrderByCreatedAtAscWithLimit(batchSize);

        for (OutboxEvent event : pending) {
            publisher.publish(event);
            event.markProcessed();
            outboxRepository.save(event);
        }
    }

}

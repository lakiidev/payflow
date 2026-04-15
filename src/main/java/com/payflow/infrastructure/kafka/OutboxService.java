package com.payflow.infrastructure.kafka;

import com.payflow.domain.model.outbox.OutboxEvent;
import com.payflow.domain.model.outbox.OutboxEventStatus;
import com.payflow.infrastructure.persistence.jpa.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<OutboxEvent> fetchAndMarkAsProcessing(Integer batchSize) {
        List<OutboxEvent> pending = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING, Limit.of(batchSize));
        pending.forEach(event->event.setStatus(OutboxEventStatus.PROCESSING));
        outboxRepository.saveAll(pending);
        return pending;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsProcessed(UUID id) {
        outboxRepository.findById(id).ifPresent(event -> {
            event.setStatus(OutboxEventStatus.PROCESSED); outboxRepository.save(event);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(UUID id) {
        outboxRepository.findById(id).ifPresent(event -> {
            event.setStatus(OutboxEventStatus.FAILED); outboxRepository.save(event);
        });
    }
}

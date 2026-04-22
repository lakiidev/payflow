package com.payflow.infrastructure.kafka;

import com.payflow.domain.model.outbox.OutboxEvent;
import com.payflow.domain.model.outbox.OutboxEventStatus;
import com.payflow.domain.repository.OutboxRepository;
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
    public List<OutboxEvent> fetchPending(Integer batchSize) {
        return outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING, Limit.of(batchSize));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsProcessed(UUID id) {
        outboxRepository.findById(id).ifPresent(event -> {
            event.setStatus(OutboxEventStatus.PROCESSED); outboxRepository.save(event);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementRetry(UUID id, String error) {
        outboxRepository.findById(id).ifPresent(event -> {
            event.setRetryCount(event.getRetryCount() + 1);
            event.setLastError(error);
            outboxRepository.save(event);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(UUID id) {
        outboxRepository.findById(id).ifPresent(event -> {
            event.setStatus(OutboxEventStatus.FAILED);
            outboxRepository.save(event);
        });
    }
}

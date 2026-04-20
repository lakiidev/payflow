package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.event.ProcessedEvent;
import com.payflow.domain.model.event.ProcessedEvent.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEventId> {
    boolean existsByIdEventIdAndIdConsumerGroup(UUID eventId, String consumerGroup);
}
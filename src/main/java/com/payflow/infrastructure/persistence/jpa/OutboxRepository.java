package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.outbox.OutboxEvent;
import com.payflow.domain.model.outbox.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByStatus(OutboxEventStatus status);
    List<OutboxEvent> findByStatusOrderByCreatedAtAscWithLimit(Integer limit);
}
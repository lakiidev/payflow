package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.outbox.OutboxEvent;
import com.payflow.domain.repository.OutboxRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxEvent, UUID>, OutboxRepository {
}
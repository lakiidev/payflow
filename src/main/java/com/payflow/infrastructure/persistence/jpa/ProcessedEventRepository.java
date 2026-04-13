package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.event.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
    boolean existsById(UUID id);
}
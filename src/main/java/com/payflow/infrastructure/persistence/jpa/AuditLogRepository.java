package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId);
}

package com.payflow.domain.model.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AuditLog {

    @Id
    @UuidGenerator
    private UUID id;

    @Column
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 50)
    private String entityType;

    @Column
    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String newValue;

    @Column(columnDefinition = "inet", insertable = false, updatable = false)
    private String ipAddress;

    @Column(columnDefinition = "text")
    private String userAgent;

    @CreationTimestamp
    private Instant createdAt;
}

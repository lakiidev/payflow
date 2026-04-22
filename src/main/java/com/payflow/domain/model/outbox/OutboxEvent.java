package com.payflow.domain.model.outbox;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="outbox_events")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter

public class OutboxEvent {
    @Id
    @UuidGenerator
    private UUID id;

    @Column( nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;
    @Setter
    @Column(name = "retry_count", nullable = false)
    private int retryCount;
    @Setter
    @Column(name = "last_error")
    private String lastError;

    @CreationTimestamp
    private Instant createdAt;

    private Instant processedAt;



}

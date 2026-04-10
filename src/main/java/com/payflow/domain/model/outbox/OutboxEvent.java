package com.payflow.domain.model.outbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="outbox_events")
@Builder
@NoArgsConstructor
@AllArgsConstructor

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

    @Column( nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;

    @CreationTimestamp
    private Instant createdAt;

    private Instant processedAt;

    public void markProcessed() {
        this.status = OutboxEventStatus.PROCESSED;
        this.processedAt = Instant.now();
    }
}

package com.payflow.domain.model.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Table(name = "processed_events")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {
    @UuidGenerator
    @Id
    private UUID uuid;

    @Column(nullable = false)
    private Instant processedAt;
}

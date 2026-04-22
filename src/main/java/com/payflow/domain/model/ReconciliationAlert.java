package com.payflow.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "reconciliation_alerts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationAlert {
    @Id
    @UuidGenerator
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Column(nullable = false)
    private String detail;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

}

package com.payflow.domain.model.ledger;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Table(name = "ledger_entries")
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class LedgerEntry {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false,updatable = false)
    private UUID transactionId;

    @Column(nullable = false,updatable = false)
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType entryType;

    @Column(nullable = false,updatable = false)
    private Long amount;

    @Column(nullable = false,updatable = false)
    private Long balanceAfter;

    @CreationTimestamp
    private Instant createdAt;
}

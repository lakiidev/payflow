package com.payflow.domain.model.transaction;


import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Table(name = "transactions")
@Entity
@NoArgsConstructor

public class Transaction {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false,updatable = false)
    private String idempotencyKey;

    @Column(nullable = false, updatable = false)
    private UUID fromWalletId;

    @Column(nullable = false, updatable = false)
    private UUID toWalletId;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant  updatedAt;

}

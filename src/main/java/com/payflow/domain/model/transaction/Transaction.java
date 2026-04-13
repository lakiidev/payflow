package com.payflow.domain.model.transaction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Table(name = "transactions")
@Entity
@NoArgsConstructor
@Getter
public class Transaction {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, updatable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TransactionType type;

    @Column(updatable = false)
    private UUID fromWalletId;

    @Column(updatable = false)
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

    @Column
    private Instant completedAt;

    @Column
    private UUID userId;

    public void complete() {
        this.status = TransactionStatus.SUCCESS;
        this.completedAt = Instant.now();
    }

    public static Transaction create(String idempotencyKey, TransactionType type, UUID fromWalletId, UUID toWalletId, Long amount, Currency currency) {
        Transaction transaction = new Transaction();
        transaction.idempotencyKey = idempotencyKey;
        transaction.type = type;
        transaction.fromWalletId = fromWalletId;
        transaction.toWalletId = toWalletId;
        transaction.amount = amount;
        transaction.currency = currency;
        transaction.status = TransactionStatus.PENDING;
        return transaction;
    }
}

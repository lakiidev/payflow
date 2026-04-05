package com.payflow.domain.model.wallet;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false,updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private WalletStatus status;

    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant  updatedAt;

    public static Wallet create(UUID userId, Currency currency){
        Wallet wallet = new Wallet();
        wallet.userId = userId;
        wallet.currency = currency;
        wallet.currentBalance = BigDecimal.ZERO;
        wallet.status = WalletStatus.ACTIVE;
        return wallet;
    }
}

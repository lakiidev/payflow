package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUserIdAndCurrency(UUID userId, Currency currency);
    Optional<Wallet> findByIdAndUserId(UUID id, UUID userId);
    List<Wallet> findAllByUserId(UUID userId);

    Optional<Wallet> findByIdAndUserIdAndStatus(UUID id, UUID userId, WalletStatus status);

}
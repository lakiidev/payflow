package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUserIdAndCurrency(UUID userId, Currency currency);
    List<Wallet> findAllByUserId(UUID userId);
}
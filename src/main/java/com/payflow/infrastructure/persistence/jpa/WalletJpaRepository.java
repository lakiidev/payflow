package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.repository.WalletRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletJpaRepository extends JpaRepository<Wallet, UUID>, WalletRepository {
    @Override
    Wallet save(Wallet wallet);
}
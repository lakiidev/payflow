package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletStatus;
import com.payflow.domain.repository.WalletRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletJpaRepository extends JpaRepository<Wallet, UUID>, WalletRepository {

}
package com.payflow.application.service;

import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletNotFoundException;
import com.payflow.domain.model.wallet.WalletStatus;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    public Wallet getActiveById(UUID walletId, UUID requestingUserId) {
        return walletRepository.findByIdAndUserIdAndStatus(walletId,requestingUserId, WalletStatus.ACTIVE).orElseThrow(
                () -> new WalletNotFoundException(walletId)
        );
    }
}

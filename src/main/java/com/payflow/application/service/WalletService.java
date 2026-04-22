package com.payflow.application.service;

import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletNotFoundException;
import com.payflow.domain.model.wallet.WalletStatus;
import com.payflow.domain.repository.WalletRepository;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    @Cacheable(value = "wallets", key = "#walletId + ':' + #requestingUserId")
    public Wallet getActiveById(UUID walletId, UUID requestingUserId) {
        return walletRepository.findByIdAndUserIdAndStatus(walletId,requestingUserId, WalletStatus.ACTIVE).orElseThrow(
                () -> new WalletNotFoundException(walletId)
        );
    }
    @CacheEvict(value = "wallets", key = "#wallet.id + ':' + #wallet.userId")
    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }
}

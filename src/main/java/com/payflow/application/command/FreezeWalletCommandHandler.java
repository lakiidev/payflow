package com.payflow.application.command;

import com.payflow.domain.model.wallet.WalletAccessDeniedException;
import com.payflow.domain.model.wallet.WalletNotFoundException;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FreezeWalletCommandHandler {

    private final WalletRepository walletRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void handle(FreezeWalletCommand command) {
        var wallet = walletRepository.findById(command.walletId())
                .orElseThrow(() -> new WalletNotFoundException(command.walletId()));

        if (!wallet.getUserId().equals(command.userId())) {
            throw new WalletAccessDeniedException(command.walletId());
        }

        wallet.freeze();
        walletRepository.save(wallet);
    }
}

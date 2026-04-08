package com.payflow.application.command;

import com.payflow.api.dto.response.WalletResponse;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletAlreadyExistsException;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateWalletCommandHandler {

    private final WalletRepository walletRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public WalletResponse handle(CreateWalletCommand command) {
        if (walletRepository.findByUserIdAndCurrency(command.userId(), command.currency()).isPresent()) {
            throw new WalletAlreadyExistsException(command.userId(), command.currency());
        }
        Wallet wallet = Wallet.create(command.userId(), command.currency());
        walletRepository.save(wallet);
        return WalletResponse.from(wallet);
    }
}

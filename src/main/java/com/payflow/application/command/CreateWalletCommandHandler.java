package com.payflow.application.command;

import com.payflow.api.dto.response.WalletResponse;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletAlreadyExistsException;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;

import java.util.Currency;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateWalletCommandHandler {

    public record Command(UUID userId, Currency currency) {}

    private final WalletRepository walletRepository;

    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class, PessimisticLockingFailureException.class},
            maxAttemptsExpression = "${payflow.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${payflow.retry.initial-interval-ms:100}",
                    multiplierExpression = "${payflow.retry.multiplier:2.0}",
                    maxDelayExpression = "${payflow.retry.max-interval-ms:1000}"
            )
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public WalletResponse handle(Command command) {
        if (walletRepository.findByUserIdAndCurrency(command.userId(), command.currency()).isPresent()) {
            throw new WalletAlreadyExistsException(command.userId(), command.currency());
        }
        Wallet wallet = Wallet.create(command.userId(), command.currency());
        walletRepository.save(wallet);
        return WalletResponse.from(wallet);
    }
}

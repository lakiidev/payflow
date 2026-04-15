package com.payflow.application.command;

import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletNotFoundException;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;

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
public class FreezeWalletCommandHandler {

    public record Command(UUID walletId, UUID userId) {}

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
    public void handle(Command command) {
        Wallet wallet = walletRepository.findByIdAndUserId(command.walletId(), command.userId())
                .orElseThrow(() -> new WalletNotFoundException(command.walletId()));

        wallet.freeze();
        walletRepository.save(wallet);
    }
}

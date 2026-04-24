package com.payflow.application.command.transactions;


import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.repository.LedgerEntryRepository;
import com.payflow.domain.repository.TransactionRepository;
import com.payflow.domain.repository.WalletRepository;
import com.payflow.infrastructure.BaseTransactionTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


class DepositIdempotencyIntegrationTest extends BaseTransactionTest {

    @Autowired TransactionRepository transactionRepository;
    @Autowired LedgerEntryRepository ledgerRepository;
    @Autowired WalletRepository walletRepository;
    @Autowired DepositCommandHandler depositHandler;

    private static final String IDEMPOTENCY_KEY = "idem-deposit-" + UUID.randomUUID();

    @Test
    void duplicateDepositWithSameKeyProducesExactlyOneTransactionAndOneLedgerEntry() {
        // Given
        DepositCommandHandler.Command command = new DepositCommandHandler.Command(
                IDEMPOTENCY_KEY, wallet.getId(), user.getId(), 5_000L
        );

        // When
        var first = depositHandler.handle(command);
        var second = depositHandler.handle(command);

        // Then
        assertThat(second.getId()).isEqualTo(first.getId());

        assertThat(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .isPresent()
                .hasValueSatisfying(tx -> assertThat(tx.getId()).isEqualTo(first.getId()));

        assertThat(ledgerRepository.findAllByTransactionId(first.getId())).hasSize(1);

        Wallet current = walletRepository.findById(wallet.getId()).orElseThrow();
        assertThat(current.getCurrentBalance()).isEqualTo(5_000L);
    }
}

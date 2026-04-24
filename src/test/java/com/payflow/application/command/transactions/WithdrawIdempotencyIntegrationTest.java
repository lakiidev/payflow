package com.payflow.application.command.transactions;

import com.payflow.application.service.WalletService;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.repository.LedgerEntryRepository;
import com.payflow.domain.repository.TransactionRepository;
import com.payflow.infrastructure.BaseTransactionTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;


class WithdrawIdempotencyIntegrationTest extends BaseTransactionTest {

    @Autowired WithdrawCommandHandler withdrawHandler;
    @Autowired DepositCommandHandler depositHandler;
    @Autowired TransactionRepository transactionRepository;
    @Autowired LedgerEntryRepository ledgerRepository;
    @Autowired WalletService walletService;

    private static final String IDEMPOTENCY_KEY = "idem-withdraw-" + UUID.randomUUID();


    @Test
    void duplicateWithdrawWithSameKeyProducesExactlyOneTransactionAndOneLedgerEntry() {
        // Given
        seedBalance(10_000L);
        WithdrawCommandHandler.Command command = new WithdrawCommandHandler.Command(
                IDEMPOTENCY_KEY, wallet.getId(), user.getId(), 3_000L
        );

        // When
        var first = withdrawHandler.handle(command);
        var second = withdrawHandler.handle(command);

        // Then
        assertThat(second.getId()).isEqualTo(first.getId());

        assertThat(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .isPresent()
                .hasValueSatisfying(tx -> assertThat(tx.getId()).isEqualTo(first.getId()));


        var ledgerEntries = ledgerRepository.findAllByTransactionId(first.getId());
        assertThat(ledgerEntries).hasSize(1);

        Wallet current = walletService.getActiveById(wallet.getId(), user.getId());
        assertThat(current.getCurrentBalance()).isEqualTo(7_000L);
    }
}
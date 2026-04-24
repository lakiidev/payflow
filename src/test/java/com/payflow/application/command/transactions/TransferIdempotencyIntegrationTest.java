package com.payflow.application.command.transactions;

import com.payflow.application.service.WalletService;
import com.payflow.domain.model.user.User;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.repository.LedgerEntryRepository;
import com.payflow.domain.repository.TransactionRepository;
import com.payflow.domain.repository.UserRepository;
import com.payflow.domain.repository.WalletRepository;
import com.payflow.infrastructure.BaseTransactionTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransferIdempotencyIntegrationTest extends BaseTransactionTest {

    @Autowired TransferCommandHandler transferHandler;
    @Autowired TransactionRepository transactionRepository;
    @Autowired LedgerEntryRepository ledgerEntryRepository;
    @Autowired WalletRepository walletRepository;
    @Autowired WalletService walletService;
    @Autowired UserRepository userRepository;

    private static final String IDEMPOTENCY_KEY = "idem-transfer-" + UUID.randomUUID();

    private User receiver;
    private Wallet destinationWallet;

    @BeforeEach
    void setupSecondWallet() {
        receiver = userRepository.save(
                User.builder()
                        .fullName("Receiver")
                        .email("receiver-" + UUID.randomUUID() + "@payflow.com")
                        .passwordHash("some-hash")
                        .build()
        );
        destinationWallet = walletService.save(
                Wallet.create(receiver.getId(), Currency.getInstance("GBP"))
        );
        seedBalance(10_000L);
    }

    @Test
    void duplicateTransferWithSameKeyProducesExactlyOneTransactionAndTwoLedgerEntries() {
        // Given
        TransferCommandHandler.Command command = new TransferCommandHandler.Command(
                IDEMPOTENCY_KEY, wallet.getId(), destinationWallet.getId(),
                user.getId(), 4_000L
        );

        // When
        var first = transferHandler.handle(command);
        var second = transferHandler.handle(command);

        // Then
        assertThat(second.getId()).isEqualTo(first.getId());

        assertThat(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .isPresent()
                .hasValueSatisfying(tx -> assertThat(tx.getId()).isEqualTo(first.getId()));

        assertThat(ledgerEntryRepository.findAllByTransactionId(first.getId())).hasSize(2);

        Wallet currentSource = walletRepository.findById(wallet.getId()).orElseThrow();
        Wallet currentDestination = walletRepository.findById(destinationWallet.getId()).orElseThrow();
        assertThat(currentSource.getCurrentBalance()).isEqualTo(6_000L);
        assertThat(currentDestination.getCurrentBalance()).isEqualTo(4_000L);
    }
}
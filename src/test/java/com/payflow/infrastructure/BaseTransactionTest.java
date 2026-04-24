package com.payflow.infrastructure;

import com.payflow.BaseIntegrationTest;
import com.payflow.application.command.transactions.DepositCommandHandler;
import com.payflow.application.service.WalletService;
import com.payflow.domain.model.user.User;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Currency;
import java.util.UUID;

public abstract class BaseTransactionTest extends BaseIntegrationTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    WalletService walletService;
    @Autowired
    DepositCommandHandler depositHandler;

    protected User user;
    protected Wallet wallet;

    @BeforeEach
    void setupUserAndWallet() {
        user = userRepository.save(
                User.builder()
                        .fullName("Test User")
                        .email("user-" + UUID.randomUUID() + "@payflow.com")
                        .passwordHash("some-hash")
                        .build()
        );
        wallet = walletService.save(Wallet.create(user.getId(), Currency.getInstance("GBP")));
    }

    protected void seedBalance(long amountCents) {
        depositHandler.handle(new DepositCommandHandler.Command(
                UUID.randomUUID().toString(), wallet.getId(), user.getId(), amountCents
        ));
    }
    public Wallet freshWallet() {
        User receiver = userRepository.save(
                User.builder()
                        .fullName("Receiver")
                        .email("receiver-" + UUID.randomUUID() + "@payflow.com")
                        .passwordHash("some-hash")
                        .build()
        );
        return walletService.save(Wallet.create(receiver.getId(), Currency.getInstance("GBP")));
    }
}
package com.payflow.application.query.wallet;

import com.payflow.api.dto.response.BalanceResponse;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletAccessDeniedException;
import com.payflow.domain.model.wallet.WalletNotFoundException;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWalletBalanceQueryHandlerTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private GetWalletBalanceQueryHandler handler;

    private static final Currency GBP = Currency.getInstance("GBP");

    @Test
    void shouldReturnBalanceForWalletOwner() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = Wallet.create(userId, GBP);
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));

        BalanceResponse response = handler.handle(new GetWalletBalanceQueryHandler.Query(wallet.getId(), userId));

        assertThat(response.balanceCents()).isZero();
        assertThat(response.currency()).isEqualTo(GBP);
        assertThat(response.walletId()).isEqualTo(wallet.getId());
    }

    @Test
    void shouldThrowWhenWalletNotFound() {
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetWalletBalanceQueryHandler.Query(walletId, UUID.randomUUID())))
                .isInstanceOf(WalletNotFoundException.class);
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnWallet() {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Wallet wallet = Wallet.create(ownerId, GBP);
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> handler.handle(new GetWalletBalanceQueryHandler.Query(wallet.getId(), otherUserId)))
                .isInstanceOf(WalletAccessDeniedException.class);
    }
}

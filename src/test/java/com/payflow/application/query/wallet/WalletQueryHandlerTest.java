package com.payflow.application.query.wallet;

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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletQueryHandlerTest {

    @Mock
    WalletRepository walletRepository;

    @InjectMocks
    WalletQueryHandler walletQueryHandler;

    @Test
    void shouldThrowWhenWalletNotFound() {
        // Given
        when(walletRepository.findById(any())).thenReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() ->
                walletQueryHandler.handle(new WalletQueryHandler.GetByIdQuery(UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(WalletNotFoundException.class);
    }

    @Test
    void shouldThrowWhenWalletBelongsToDifferentUser() {
        // Given
        Wallet wallet = Wallet.create(UUID.randomUUID(), Currency.getInstance("EUR"));
        when(walletRepository.findById(any())).thenReturn(Optional.of(wallet));

        // When + Then
        assertThatThrownBy(() ->
                walletQueryHandler.handle(new WalletQueryHandler.GetByIdQuery(wallet.getId(), UUID.randomUUID())))
                .isInstanceOf(WalletAccessDeniedException.class);
    }
}
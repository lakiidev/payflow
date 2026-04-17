package com.payflow.application.query.wallet;

import com.payflow.domain.model.wallet.WalletNotFoundException;
import com.payflow.domain.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void shouldThrowWhenWalletNotFoundOrNotOwnedByUser() {
        when(walletRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                walletQueryHandler.handle(new WalletQueryHandler.GetByIdQuery(UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(WalletNotFoundException.class);
    }
}

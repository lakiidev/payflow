package com.payflow.application.query;


import com.payflow.application.dto.TransactionView;
import com.payflow.application.port.WalletStatementPort;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletNotFoundException;
import com.payflow.domain.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletStatementQueryHandlerTest {

    @Mock
    private WalletStatementPort walletStatementPort;

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionView transactionView;

    @InjectMocks
    private WalletStatementQueryHandler handler;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final Instant FROM = Instant.now().minusSeconds(3600);
    private static final Instant TO = Instant.now();

    private WalletStatementQueryHandler.Query query() {
        return new WalletStatementQueryHandler.Query(USER_ID, WALLET_ID, FROM, TO);
    }

    private Wallet walletOwnedByUser() {
        return Wallet.create(USER_ID, Currency.getInstance("EUR"));
    }

    @Test
    void shouldReturnStatementWhenUserOwnsWallet() {
        // Given
        Stream<TransactionView> expected = Stream.of(transactionView);
        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(walletOwnedByUser()));
        when(walletStatementPort.findStatementRows(WALLET_ID, FROM, TO)).thenReturn(expected);

        // When
        Stream<TransactionView> result = handler.handle(query());

        // Then
        assertThat(result).isSameAs(expected);
    }

    @Test
    void shouldThrowWhenWalletNotFound() {
        // Given
        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> handler.handle(query()))
                .isInstanceOf(WalletNotFoundException.class);

        verifyNoInteractions(walletStatementPort);
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnWallet() {
        // Given
        Wallet walletOwnedByOther = Wallet.create(UUID.randomUUID(), Currency.getInstance("EUR"));
        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(walletOwnedByOther));

        // When / Then
        assertThatThrownBy(() -> handler.handle(query()))
                .isInstanceOf(WalletNotFoundException.class);

        verifyNoInteractions(walletStatementPort);
    }
}
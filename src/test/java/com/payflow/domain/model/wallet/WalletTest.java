package com.payflow.domain.model.wallet;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class WalletTest {

    @Test
    void create_shouldCreateWalletWithZeroBalance() {
        //When
        Wallet wallet = Wallet.create(UUID.randomUUID(), Currency.getInstance("GBP"));

        //Then
        assertThat(wallet.getCurrentBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(wallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
        assertThat(wallet.getCurrency()).isEqualTo(Currency.getInstance("GBP"));
    }
}
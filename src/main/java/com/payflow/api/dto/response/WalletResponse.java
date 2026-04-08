package com.payflow.api.dto.response;

import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletStatus;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        Currency currency,
        Long balance,
        WalletStatus status,
        Instant createdAt
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getCurrency(),
                wallet.getCurrentBalance(),
                wallet.getStatus(),
                wallet.getCreatedAt()
        );
    }
}
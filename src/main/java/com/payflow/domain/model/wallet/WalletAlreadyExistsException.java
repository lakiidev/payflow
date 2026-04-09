package com.payflow.domain.model.wallet;

import java.util.Currency;
import java.util.UUID;

public class WalletAlreadyExistsException extends RuntimeException {
    public WalletAlreadyExistsException(UUID userId, Currency currency) {
        super("Wallet for currency " + currency.getCurrencyCode() + " already exists for user: " + userId);
    }
}

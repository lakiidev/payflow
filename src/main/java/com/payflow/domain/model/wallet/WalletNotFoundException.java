package com.payflow.domain.model.wallet;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(UUID userId) {
        super("Wallet not found for user: " + userId);
    }
}

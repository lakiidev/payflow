package com.payflow.domain.model.wallet;

import java.util.UUID;

public class WalletAlreadyFrozenException extends RuntimeException {
    public WalletAlreadyFrozenException(UUID id) {
        super(
                "Wallet is already frozen: " + id
        );
    }
}
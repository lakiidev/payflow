package com.payflow.domain.model.transaction;

import java.util.UUID;

public class InvalidWalletOperationException extends RuntimeException {
    public InvalidWalletOperationException(UUID walletId) {
        super("Invalid wallet operation for wallet: " + walletId);
    }
}

package com.payflow.domain.model.wallet;

import java.util.UUID;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(UUID walletId, Long amount) {
        super("Insufficient balance for wallet: " + walletId + " with amount: " + amount);
    }
}

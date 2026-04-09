package com.payflow.domain.model.wallet;

import java.util.UUID;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(UUID uuid, Long amount) {
        super("Insufficient balance for user: " + uuid + " with amount: " + amount + "");
    }
}

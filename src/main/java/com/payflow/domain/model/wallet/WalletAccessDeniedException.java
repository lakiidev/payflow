package com.payflow.domain.model.wallet;

import java.util.UUID;

public class WalletAccessDeniedException extends RuntimeException {
    public WalletAccessDeniedException(UUID walletId) {
        super("Access denied to wallet: " + walletId);
    }
}

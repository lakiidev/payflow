package com.payflow.infrastructure.reconciliation;

import java.util.UUID;

public record WalletDiscrepancy(
        UUID walletId,
        long  cachedBalance,
        long  computedBalance
) {
}

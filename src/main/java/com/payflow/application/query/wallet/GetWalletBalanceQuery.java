package com.payflow.application.query.wallet;

import java.util.UUID;

public record GetWalletBalanceQuery(UUID walletId, UUID requestingUserId) {}

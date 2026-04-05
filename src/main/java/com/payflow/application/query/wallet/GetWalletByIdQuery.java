package com.payflow.application.query.wallet;

import java.util.UUID;

public record GetWalletByIdQuery(UUID walletId, UUID requestingUserId) {}


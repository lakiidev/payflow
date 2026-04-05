package com.payflow.application.query.walelt;

import java.util.UUID;

public record GetWalletByIdQuery(UUID walletId, UUID requestingUserId) {}


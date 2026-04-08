package com.payflow.api.dto.response;

import java.util.Currency;
import java.util.UUID;

public record BalanceResponse(UUID walletId, Long balanceCents, Currency currency) {}

package com.payflow.api.dto.response;

import java.time.Instant;

public record BalanceHistoryResponse(Instant interval,
                                     long lastBalance) {
}

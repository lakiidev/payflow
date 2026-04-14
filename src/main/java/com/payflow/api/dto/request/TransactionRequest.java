package com.payflow.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public class TransactionRequest {
    public record DepositRequest(
            UUID toWalletId,
            @NotNull @Positive Long amount,          // cents
            String currency,
            @NotNull UUID idempotencyKey
    ) {}

    public  record WithdrawRequest(
            UUID fromWalletId,
            @NotNull @Positive Long amount,          // cents
            String currency,
            @NotNull UUID idempotencyKey
    ) {}

    public record TransferRequest(
            UUID fromWalletId,
            UUID toWalletId,
            @NotNull @Positive Long amount,          // cents
            String currency,
            @NotNull UUID idempotencyKey
    ) {}
}

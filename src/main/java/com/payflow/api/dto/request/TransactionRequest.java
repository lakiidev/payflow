package com.payflow.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public class TransactionRequest {
    public record DepositRequest(
            @NotNull UUID toWalletId,
            @NotNull @Positive Long amount,          // cents
            String currency,
            @NotNull UUID idempotencyKey
    ) {}

    public  record WithdrawRequest(
            @NotNull UUID fromWalletId,
            @NotNull @Positive Long amount,          // cents
            String currency,
            @NotNull UUID idempotencyKey
    ) {}

    public record TransferRequest(
            @NotNull UUID fromWalletId,
            @NotNull UUID toWalletId,
            @NotNull @Positive Long amount,          // cents
            String currency,
            @NotNull UUID idempotencyKey
    ) {}
}

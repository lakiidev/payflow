package com.payflow.api.dto.request;

import java.util.UUID;

public class TransactionRequest {
    public record DepositRequest(
            UUID toWalletId,
            Long amount,          // cents
            UUID idempotencyKey
    ) {}

    public  record WithdrawRequest(
            UUID fromWalletId,
            Long amount,          // cents
            UUID idempotencyKey
    ) {}

    public record TransferRequest(
            UUID fromWalletId,
            UUID toWalletId,
            Long amount,          // cents
            UUID idempotencyKey
    ) {}
}

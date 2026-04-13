package com.payflow.api.dto.response;

import com.payflow.domain.model.transaction.Transaction;
import com.payflow.domain.model.transaction.TransactionStatus;
import com.payflow.domain.model.transaction.TransactionType;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID fromWalletId,
        TransactionType type,
        long amount,
        Currency currency,
        TransactionStatus status,
        UUID toWalletId,
        Instant createdAt
) {
    public static TransactionResponse from(Transaction projection) {
        return new TransactionResponse(
                projection.getId(),
                projection.getFromWalletId(),
                projection.getType(),
                projection.getAmount(),
                projection.getCurrency(),
                projection.getStatus(),
                projection.getToWalletId(),
                projection.getCreatedAt()
        );
    }
}
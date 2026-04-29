package com.payflow.application.dto;

import com.payflow.domain.model.ledger.EntryType;

import java.time.Instant;
import java.util.UUID;

public record TransactionView(UUID transactionId,
                              Instant createdAt,
                              EntryType entryType,      // DEBIT / CREDIT
                              long amount,
                              long balanceAfter) {

}

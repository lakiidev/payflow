package com.payflow.application.port;

import com.payflow.application.dto.TransactionView;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WalletStatementPort {
    List<TransactionView> findStatementRows(UUID walletId, Instant from, Instant to);
}

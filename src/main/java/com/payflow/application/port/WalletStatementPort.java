package com.payflow.application.port;

import com.payflow.application.dto.TransactionView;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

public interface WalletStatementPort {
    /**
     * Returns statement rows as a lazily-consumed stream so large histories do not need
     * to be fully materialized in memory.
     *
     * <p>The caller is responsible for consuming this stream within the surrounding
     * transaction and closing it when finished, for example via try-with-resources.
     */
    Stream<TransactionView> findStatementRows(UUID walletId, Instant from, Instant to);
}

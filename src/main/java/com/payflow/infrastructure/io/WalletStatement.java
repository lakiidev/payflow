package com.payflow.infrastructure.io;

import com.payflow.application.dto.TransactionView;
import com.payflow.application.port.WalletStatementPort;
import com.payflow.infrastructure.persistence.jpa.LedgerEntryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class WalletStatement implements WalletStatementPort {
    private final LedgerEntryJpaRepository repository;
    @Override
    public List<TransactionView> findStatementRows(UUID walletId, Instant from, Instant to) {
        return repository.findStatementRows(walletId, from, to);
    }
}

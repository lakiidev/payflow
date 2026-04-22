package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.wallet.Wallet;
import com.payflow.infrastructure.reconciliation.WalletDiscrepancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletReconciliationRepository extends JpaRepository<Wallet, UUID> {

    @Query("""
        SELECT new com.payflow.infrastructure.reconciliation.WalletDiscrepancy(
            w.id,
            w.currentBalance,
            COALESCE((
                SELECT SUM(CASE WHEN l.entryType = com.payflow.domain.model.ledger.EntryType.CREDIT THEN l.amount ELSE -l.amount END)
                FROM LedgerEntry l WHERE l.walletId = w.id
            ), 0)
        )
        FROM Wallet w
        WHERE w.currentBalance != COALESCE((
            SELECT SUM(CASE WHEN l.entryType = com.payflow.domain.model.ledger.EntryType.CREDIT THEN l.amount ELSE -l.amount END)
            FROM LedgerEntry l WHERE l.walletId = w.id
        ), 0)
        """)
    List<WalletDiscrepancy> findCacheDiscrepancies();
}

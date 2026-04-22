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
        SELECT new  com.payflow.infrastructure.reconciliation.WalletDiscrepancy(
            w.id,
            w.currentBalance,
            COALESCE(SUM(CASE WHEN l.entryType = com.payflow.domain.model.wallet.EntryType.CREDIT THEN l.amount ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN l.entryType = com.payflow.domain.model.wallet.EntryType.DEBIT THEN l.amount ELSE 0 END), 0)
        )
        FROM Wallet w
        LEFT JOIN LedgerEntry l ON l.walletId = w.id
        GROUP BY w.id, w.currentBalance
        HAVING w.currentBalance <> (
            COALESCE(SUM(CASE WHEN l.entryType = com.payflow.domain.model.wallet.EntryType.CREDIT THEN l.amount ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN l.entryType = com.payflow.domain.model.wallet.EntryType.DEBIT THEN l.amount ELSE 0 END), 0)
        )
        """)
    List<WalletDiscrepancy> findCacheDiscrepancies();
}

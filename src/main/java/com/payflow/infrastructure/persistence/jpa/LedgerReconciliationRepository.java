package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.ledger.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LedgerReconciliationRepository extends JpaRepository<LedgerEntry, UUID> {

    @Query(value = """
    SELECT 
        COALESCE(SUM(amount) FILTER (WHERE entry_type = 'CREDIT'), 0) -
        COALESCE(SUM(amount) FILTER (WHERE entry_type = 'DEBIT'), 0)
    FROM ledger_entries
    """, nativeQuery = true)
    long computeGlobalDelta();
}

package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.ledger.LedgerEntry;
import com.payflow.domain.repository.LedgerEntryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntry, UUID>, LedgerEntryRepository {

}
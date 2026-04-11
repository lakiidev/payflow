package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.ledger.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
}
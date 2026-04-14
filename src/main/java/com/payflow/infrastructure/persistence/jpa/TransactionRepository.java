package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction,UUID > {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Page<Transaction> findAllByUserIdOrderByCreatedAtDesc(UUID uuid, Pageable pageable);
    Optional<Transaction> findByIdAndUserId(UUID uuid, UUID userId);
}
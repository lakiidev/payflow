package com.payflow.domain.repository;

import com.payflow.domain.model.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    Page<Transaction> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);
    Transaction save(Transaction transaction);
}
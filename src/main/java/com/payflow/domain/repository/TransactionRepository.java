package com.payflow.domain.repository;

import com.payflow.domain.model.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface TransactionRepository {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    Page<Transaction> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);
    Transaction save(Transaction transaction);
    Stream<Transaction> findByWalletIdBetween(UUID walletId, Instant from, Instant to);
}
package com.payflow.application.service;

import com.payflow.domain.model.transaction.Transaction;
import com.payflow.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final TransactionRepository transactionRepository;
    public Optional<Transaction> findDuplicate(String idempotencyKey) {
        return transactionRepository.findByIdempotencyKey(idempotencyKey);
    }

    public Transaction deduplicateOrSave(Transaction tx) {
        try {
            return transactionRepository.save(tx);
        } catch (DataIntegrityViolationException e) {
            // Concurrent request with the same idempotency key hit the unique constraint
            // Re-fetch and return the winner's record
            return transactionRepository.findByIdempotencyKey(tx.getIdempotencyKey())
                    .orElseThrow(() -> new IllegalStateException(
                            "Idempotency constraint violated but record not found", e));
        }
    }
}

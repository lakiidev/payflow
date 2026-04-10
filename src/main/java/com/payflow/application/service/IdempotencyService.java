package com.payflow.application.service;

import com.payflow.domain.model.transaction.Transaction;
import com.payflow.infrastructure.persistence.jpa.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final TransactionRepository transactionRepository;
    public Optional<Transaction> findDuplicate(String idempotencyKey) {
        return transactionRepository.findByIdempotencyKey(idempotencyKey);
    }
}

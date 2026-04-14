package com.payflow.application.query;

import com.payflow.api.dto.response.TransactionResponse;
import com.payflow.domain.model.transaction.TransactionNotFoundException;
import com.payflow.infrastructure.persistence.jpa.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionQueryHandler {

    public record GetTransactionsQuery(UUID userId, Pageable pageable) {}
    public record GetTransactionQuery(UUID transactionId, UUID userId) {}

    private final TransactionRepository repository;

    @Transactional(readOnly = true)
    public Page<TransactionResponse> handle(GetTransactionsQuery query) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(query.userId(), query.pageable())
                .map(TransactionResponse::from);
    }

    @Transactional(readOnly = true)
    public TransactionResponse handle(GetTransactionQuery query) {
        return TransactionResponse.from(
                repository.findByIdAndUserId(query.transactionId(), query.userId())
                .orElseThrow(() ->
                        new TransactionNotFoundException(query.transactionId())));
    }

}
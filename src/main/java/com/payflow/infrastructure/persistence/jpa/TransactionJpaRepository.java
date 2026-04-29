package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.transaction.Transaction;
import com.payflow.domain.repository.TransactionRepository;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE;

public interface TransactionJpaRepository extends JpaRepository<Transaction,UUID >, TransactionRepository {
    @Override
    Transaction save(Transaction transaction);

    @Query("SELECT t FROM Transaction t WHERE (t.fromWalletId = :walletId OR t.toWalletId = :walletId) AND t.createdAt BETWEEN :from AND :to")
    @QueryHints(@QueryHint(name = HINT_FETCH_SIZE, value = "1000"))
    @Override
    Stream<Transaction> findByWalletIdBetween(
            @Param("walletId") UUID walletId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
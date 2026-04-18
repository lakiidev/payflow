package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.transaction.Transaction;
import com.payflow.domain.repository.TransactionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<Transaction,UUID >, TransactionRepository {
}
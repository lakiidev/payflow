package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.ReconciliationAlert;
import com.payflow.domain.repository.ReconciliationAlertRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReconciliationAlertJpaRepository extends JpaRepository<ReconciliationAlert, UUID>, ReconciliationAlertRepository {
}

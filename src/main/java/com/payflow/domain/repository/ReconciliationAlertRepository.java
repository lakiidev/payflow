package com.payflow.domain.repository;

import com.payflow.domain.model.ReconciliationAlert;

import java.util.List;

public interface ReconciliationAlertRepository {
    ReconciliationAlert save(ReconciliationAlert reconciliationAlert);
    List<ReconciliationAlert> findAll();
    void deleteAll();
}
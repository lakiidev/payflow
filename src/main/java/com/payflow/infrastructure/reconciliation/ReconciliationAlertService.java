package com.payflow.infrastructure.reconciliation;

import com.payflow.domain.model.AlertType;
import com.payflow.domain.model.ReconciliationAlert;
import com.payflow.domain.repository.ReconciliationAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationAlertService {

    private final ReconciliationAlertRepository alertRepository;

    public void onGlobalImbalance(long delta) {
        // Week 3: wire to email
        log.error("[ALERT] Global ledger imbalance: delta={}", delta);
        alertRepository.save(ReconciliationAlert.builder()
                .type(AlertType.GLOBAL_IMBALANCE)
                .detail("Global ledger delta: " + delta)
                .build());
    }

    public void onWalletDiscrepancies(List<WalletDiscrepancy> discrepancies) {
        // Week 3: wire to email
        log.error("[ALERT] {} wallet cache discrepancies detected", discrepancies.size());
        discrepancies.forEach(d ->
                alertRepository.save(ReconciliationAlert.builder()
                        .type(AlertType.WALLET_CACHE_DISCREPANCY)
                        .detail("walletId=%s cached=%d computed=%d"
                                .formatted(d.walletId(), d.cachedBalance(), d.computedBalance()))
                        .build())
        );
    }
}

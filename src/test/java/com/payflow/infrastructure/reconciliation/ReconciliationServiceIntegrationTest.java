package com.payflow.infrastructure.reconciliation;

import com.payflow.BaseIntegrationTest;
import com.payflow.application.service.WalletService;
import com.payflow.domain.model.AlertType;
import com.payflow.domain.model.ReconciliationAlert;
import com.payflow.domain.model.ledger.LedgerEntry;
import com.payflow.domain.model.transaction.Transaction;
import com.payflow.domain.model.transaction.TransactionType;
import com.payflow.domain.model.user.User;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.repository.LedgerEntryRepository;
import com.payflow.domain.repository.ReconciliationAlertRepository;
import com.payflow.infrastructure.persistence.jpa.TransactionJpaRepository;
import com.payflow.infrastructure.persistence.jpa.UserJpaRepository;
import com.payflow.infrastructure.persistence.jpa.WalletJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static com.payflow.domain.model.ledger.EntryType.CREDIT;
import static com.payflow.domain.model.ledger.EntryType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;
class ReconciliationServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired private ReconciliationService reconciliationService;
    @Autowired private WalletJpaRepository walletRepository;
    @Autowired private WalletService walletService;
    @Autowired private LedgerEntryRepository ledgerEntryRepository;
    @Autowired private ReconciliationAlertRepository alertRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private UserJpaRepository userRepository;
    @Autowired private TransactionJpaRepository transactionRepository;

    private User testUser;
    private Wallet testWallet;
    private Transaction testTransaction;



    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        ledgerEntryRepository.deleteAll();
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .fullName("Test User")
                .email("test@payflow.com")
                .passwordHash("hashedpassword")
                .build());

        testWallet = walletService.save(Wallet.create(
                testUser.getId(),
                Currency.getInstance("GBP")
        ));

        testTransaction = transactionRepository.save(Transaction.create(
                UUID.randomUUID().toString(),
                TransactionType.DEPOSIT,
                null,
                testWallet.getId(),
                1000L,
                Currency.getInstance("GBP"),
                testUser.getId()
        ));
    }

    private LedgerEntry creditEntryWith1000(long balanceAfter) {
        return LedgerEntry.builder()
                .transactionId(testTransaction.getId())
                .walletId(testWallet.getId())
                .amount(1000L)
                .entryType(CREDIT)
                .balanceAfter(balanceAfter)
                .build();
    }

    private LedgerEntry debitEntryWith(long amount,long balanceAfter) {
        return LedgerEntry.builder()
                .transactionId(testTransaction.getId())
                .walletId(testWallet.getId())
                .amount(amount)
                .entryType(DEBIT)
                .balanceAfter(balanceAfter)
                .build();
    }

    @Test
    void reconcileShouldNotCreateAlertWhenLedgerIsBalanced() {
        // Given — CREDIT 1000, DEBIT 1000, delta = 0
        ledgerEntryRepository.save(creditEntryWith1000(1000L));
        ledgerEntryRepository.save(debitEntryWith(1000L,0L));

        // When
        reconciliationService.reconcile();

        // Then
        assertThat(alertRepository.findAll()).isEmpty();
    }

    @Test
    void reconcileShouldCreateAlertOnGlobalImbalance() {
        // Given — CREDIT 1000, DEBIT 500, delta = 500 (global imbalance)
        // wallet cache set to 500 to avoid triggering cache discrepancy too
        ledgerEntryRepository.save(creditEntryWith1000(1000L));
        ledgerEntryRepository.save(debitEntryWith(500L,500L));
        jdbcTemplate.update("UPDATE wallets SET current_balance = ? WHERE id = ?", 500L, testWallet.getId());

        // When
        reconciliationService.reconcile();

        // Then
        List<ReconciliationAlert> alerts = alertRepository.findAll();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.getFirst().getType()).isEqualTo(AlertType.GLOBAL_IMBALANCE);
    }

    @Test
    void reconcileShouldCreateAlertOnWalletCacheDiscrepancy() {
        // Given — ledger is globally balanced (1000 CREDIT, 1000 DEBIT)
        // but wallet cache says 800 instead of the correct 1000 net
        ledgerEntryRepository.save(creditEntryWith1000(1000L));
        ledgerEntryRepository.save(
                debitEntryWith(1000L,1000L)
        );
        jdbcTemplate.update("UPDATE wallets SET current_balance = ? WHERE id = ?", 800L, testWallet.getId());

        // When
        reconciliationService.reconcile();

        // Then
        List<ReconciliationAlert> alerts = alertRepository.findAll();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.getFirst().getType()).isEqualTo(AlertType.WALLET_CACHE_DISCREPANCY);
        assertThat(alerts.getFirst().getDetail()).contains(testWallet.getId().toString());
    }
}
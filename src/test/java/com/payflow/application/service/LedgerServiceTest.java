package com.payflow.application.service;

import com.payflow.domain.model.ledger.EntryType;
import com.payflow.domain.model.ledger.LedgerEntry;
import com.payflow.domain.model.transaction.Transaction;
import com.payflow.domain.model.transaction.TransactionType;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.repository.LedgerEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private LedgerService ledgerService;

    private static final UUID USER_ID   = UUID.randomUUID();
    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final Currency EUR   = Currency.getInstance("EUR");

    private Transaction deposit() {
        return Transaction.create("idem-key-1", TransactionType.DEPOSIT,
                null, WALLET_ID, 5000L, EUR, USER_ID);
    }

    private Transaction withdrawal() {
        return Transaction.create("idem-key-1", TransactionType.WITHDRAW,
                WALLET_ID, null, 3000L, EUR, USER_ID);
    }

    private Wallet walletWithBalance(long balance) {
        Wallet wallet = Wallet.create(USER_ID, EUR);
        wallet.credit(balance);
        return wallet;
    }


    @Test
    void shouldSaveCreditEntryWithCorrectFields() {
        // Given
        Transaction tx     = deposit();
        Wallet wallet      = walletWithBalance(10_000L);

        // When
        ledgerService.createCreditEntry(tx, wallet, 3000L);

        // Then
        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerEntryRepository).save(captor.capture());
        LedgerEntry entry = captor.getValue();

        assertThat(entry.getEntryType()).isEqualTo(EntryType.CREDIT);
        assertThat(entry.getAmount()).isEqualTo(3000L);
        assertThat(entry.getBalanceAfter()).isEqualTo(13_000L);   // 10_000 + 3_000
        assertThat(entry.getWalletId()).isEqualTo(wallet.getId());
        assertThat(entry.getTransactionId()).isEqualTo(tx.getId());
    }



    @Test
    void shouldSaveDebitEntryWithCorrectFields() {
        // Given
        Transaction tx     = withdrawal();
        Wallet wallet      = walletWithBalance(10_000L);

        // When
        ledgerService.createDebitEntry(tx, wallet, 3000L);

        // Then
        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerEntryRepository).save(captor.capture());
        LedgerEntry entry = captor.getValue();

        assertThat(entry.getEntryType()).isEqualTo(EntryType.DEBIT);
        assertThat(entry.getAmount()).isEqualTo(3000L);
        assertThat(entry.getBalanceAfter()).isEqualTo(7_000L);    // 10_000 - 3_000
        assertThat(entry.getWalletId()).isEqualTo(wallet.getId());
        assertThat(entry.getTransactionId()).isEqualTo(tx.getId());
    }
}

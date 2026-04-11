package com.payflow.application.command;

import com.payflow.application.service.IdempotencyService;
import com.payflow.application.service.LedgerService;
import com.payflow.application.service.WalletService;
import com.payflow.domain.model.transaction.CurrencyMismatchException;
import com.payflow.domain.model.transaction.Transaction;
import com.payflow.domain.model.transaction.TransactionType;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.infrastructure.kafka.TransactionEventPublisher;
import com.payflow.infrastructure.persistence.jpa.TransactionRepository;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferCommandHandler {
    private final WalletService walletService;

    public record Command(
            String idempotencyKey,
            UUID sourceWalletId,
            UUID destinationWalletId,
            UUID requestingUserId,
            long amountCents
    ) {}

    private final IdempotencyService idempotencyService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final TransactionEventPublisher eventPublisher;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction handle(Command command) {
        // STEP 1: Idempotency
        return idempotencyService.findDuplicate(command.idempotencyKey())
                .orElseGet(() -> processNew(command));
    }

    private Transaction processNew(Command command) {
        // STEP 2: Validate — no self-transfer
        if (command.sourceWalletId().equals(command.destinationWalletId())) {
            throw new IllegalArgumentException("Cannot transfer to the same wallet");
        }

        // STEP 3: Load both wallets — only a source needs ownership check
        Wallet sourceWallet = walletService.getActiveById(command.sourceWalletId(), command.requestingUserId());
        Wallet destionationWallet = walletService.getActiveById(command.destinationWalletId(), command.requestingUserId());
        if (!sourceWallet.getCurrency().equals(destionationWallet.getCurrency())) {
            throw new CurrencyMismatchException(sourceWallet.getCurrency(), destionationWallet.getCurrency());
        }


        // STEP 4: Create a PENDING transaction record
        Transaction tx = Transaction.create(
                command.idempotencyKey(),
                TransactionType.TRANSFER,
                command.sourceWalletId(),
                command.destinationWalletId(),
                command.amountCents(),
                sourceWallet.getCurrency()
        );
        tx = idempotencyService.deduplicateOrSave(tx);

        // STEP 5: Debit source first — throws InsufficientBalanceException if needed
        sourceWallet.debit(command.amountCents());
        walletRepository.save(sourceWallet);
        ledgerService.createDebitEntry(tx, sourceWallet, command.amountCents());

        // STEP 6: Credit destination
        ledgerService.createCreditEntry(tx, destionationWallet, command.amountCents());
        destionationWallet.credit(command.amountCents());
        walletRepository.save(destionationWallet);

        // STEP 7: Mark complete and persist
        tx.complete();
        eventPublisher.publishTransactionCreated(tx);
        return transactionRepository.save(tx);
    }
}

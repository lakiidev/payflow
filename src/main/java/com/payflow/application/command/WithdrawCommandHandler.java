package com.payflow.application.command;


import com.payflow.application.service.IdempotencyService;
import com.payflow.application.service.LedgerService;
import com.payflow.application.service.WalletService;
import com.payflow.domain.model.transaction.Transaction;
import com.payflow.domain.model.transaction.TransactionType;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.infrastructure.kafka.TransactionOutboxWriter;
import com.payflow.infrastructure.persistence.jpa.TransactionRepository;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WithdrawCommandHandler {

    private final WalletService walletService;
    private final WalletRepository walletRepository;

    public record Command(
            String idempotencyKey,
            UUID walletId,
            UUID requestingUserId,
            long amountCents
    ) {}

    private final IdempotencyService idempotencyService;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final TransactionOutboxWriter eventPublisher;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction handle(Command command) {
        // STEP 1: Idempotency
        return idempotencyService.findDuplicate(command.idempotencyKey())
                .orElseGet(() -> processNew(command));
    }

    private Transaction processNew(Command command) {
        // STEP 2: Load & validate wallet — ownership + active status
        Wallet wallet = walletService.getActiveById(command.walletId(),command.requestingUserId());


        // STEP 3: Create a PENDING transaction record
        Transaction tx = Transaction.create(
                command.idempotencyKey(),
                TransactionType.WITHDRAW,
                command.walletId(),
                null,
                command.amountCents(),
                wallet.getCurrency()
        );
        tx = idempotencyService.deduplicateOrSave(tx);

        // STEP 5: debit first - checks balance AND updates currentBalance
        wallet.debit(command.amountCents());
        walletRepository.save(wallet);

        // STEP 4: Ledger entry after = balanceAfter is now correct
        ledgerService.createDebitEntry(tx, wallet, command.amountCents());

        // STEP 6: Mark complete and persist
        tx.complete();
        eventPublisher.publishTransactionCreated(tx);
        return transactionRepository.save(tx);
    }
}

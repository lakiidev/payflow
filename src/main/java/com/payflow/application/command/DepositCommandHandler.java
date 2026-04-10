package com.payflow.application.command;

import com.payflow.application.service.IdempotencyService;
import com.payflow.application.service.LedgerService;
import com.payflow.application.service.WalletService;
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
public class DepositCommandHandler {

    private final WalletService walletService;

    public record Command(
            String idempotencyKey,
            UUID walletId,
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
        // STEP 2: Load & validate wallet — ownership + active status
        Wallet wallet = walletService.getActiveById(command.walletId(), command.requestingUserId());

        // STEP 3: No balance check for deposit

        // STEP 4: Create a PENDING transaction record
        Transaction tx = Transaction.create(
                command.idempotencyKey(),
                TransactionType.DEPOSIT,
                null,
                command.walletId(),
                command.amountCents(),
                wallet.getCurrency()
        );
        transactionRepository.save(tx);

        // STEP 5: Ledger entry first (source of truth)
        ledgerService.createCreditEntry(tx, wallet, command.amountCents());

        // STEP 6: Update cached balance after the ledger is written
        wallet.credit(command.amountCents());

        // STEP 7: Mark complete and persist
        tx.complete();
        eventPublisher.publishTransactionCreated(tx);
        return transactionRepository.save(tx);
    }
}

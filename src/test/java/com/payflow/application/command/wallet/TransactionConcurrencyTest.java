package com.payflow.application.command.wallet;

import com.payflow.application.command.transactions.DepositCommandHandler;
import com.payflow.application.command.transactions.TransferCommandHandler;
import com.payflow.application.command.transactions.WithdrawCommandHandler;
import com.payflow.domain.model.wallet.InsufficientBalanceException;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.repository.WalletRepository;
import com.payflow.infrastructure.BaseTransactionTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionConcurrencyTest extends BaseTransactionTest {
    @Autowired
    DepositCommandHandler depositHandler;
    @Autowired
    WithdrawCommandHandler withdrawHandler;
    @Autowired
    TransferCommandHandler transferHandler;
    @Autowired
    WalletRepository walletRepository;

    private record ConcurrencyHarness(CountDownLatch ready,
                                      CountDownLatch start) {
        ConcurrencyHarness(int threadCount) {
            this(new CountDownLatch(threadCount), new CountDownLatch(1));
        }
    }

    private static final int THREAD_COUNT = 10;

    private ExecutorService executor(){ return Executors.newFixedThreadPool(THREAD_COUNT);}
    private ConcurrencyHarness harness() { return new ConcurrencyHarness(THREAD_COUNT);}

    private void awaitAndFire(ConcurrencyHarness harness,
                              ExecutorService executor) throws InterruptedException {
        boolean ready = harness.ready.await(30, TimeUnit.SECONDS);
        assertThat(ready).as("threads did not become ready within timeout").isTrue();
        harness.start.countDown();
        executor.shutdown();
        boolean finished = executor.awaitTermination(60, TimeUnit.SECONDS);
        assertThat(finished).as("threads did not finish within timeout — possible deadlock").isTrue();
    }


    @Test
    void concurrentDepositsOnSameWalletProduceConsistentBalance() throws InterruptedException {
        // Given
        long depositAmount = 1_000L;
        List<Long> successful = new CopyOnWriteArrayList<>();
        ExecutorService exec = executor();
        ConcurrencyHarness h = harness();

        // When
        for (int i = 0; i < THREAD_COUNT; i++) {
            exec.submit(() -> {
                try {
                    h.ready.countDown();
                    h.start.await();
                    depositHandler.handle(new DepositCommandHandler.Command(
                            UUID.randomUUID().toString(), wallet.getId(), user.getId(), depositAmount
                    ));
                    successful.add(depositAmount);
                } catch (ObjectOptimisticLockingFailureException |
                         PessimisticLockingFailureException _) {
                    // exhausted retries — expected under contention
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        awaitAndFire(h, exec);

        // Then
        assertThat(successful)
                .as("at least one deposit must succeed")
                .isNotEmpty();

        Wallet current = walletRepository.findById(wallet.getId()).orElseThrow();

        assertThat(current.getCurrentBalance())
                .isEqualTo(successful.stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void concurrentMixedDepositsAndWithdrawalsNeverCorruptBalance() throws InterruptedException {
        // Given
        long initialBalance = 10_000L;
        seedBalance(initialBalance);

        long depositAmount = 1_000L;
        long withdrawAmount = 500L;
        List<Long> successfulDeposits = new CopyOnWriteArrayList<>();
        List<Long> successfulWithdrawals = new CopyOnWriteArrayList<>();
        ExecutorService exec = executor();
        ConcurrencyHarness h = harness();

        // When
        for (int i = 0; i < THREAD_COUNT; i++) {
            boolean isDeposit = i % 2 == 0;
            exec.submit(() -> {
                try {
                    h.ready.countDown();
                    h.start.await();
                    String key = UUID.randomUUID().toString();
                    if (isDeposit) {
                        depositHandler.handle(new DepositCommandHandler.Command(
                                key, wallet.getId(), user.getId(), depositAmount
                        ));
                        successfulDeposits.add(depositAmount);
                    } else {
                        withdrawHandler.handle(new WithdrawCommandHandler.Command(
                                key, wallet.getId(), user.getId(), withdrawAmount
                        ));
                        successfulWithdrawals.add(withdrawAmount);
                    }
                } catch (ObjectOptimisticLockingFailureException |
                         PessimisticLockingFailureException _) {
                    // exhausted retries — expected under contention
                } catch (InsufficientBalanceException _) {
                    // valid business rejection — not corruption
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        awaitAndFire(h, exec);

        // Then
        assertThat(successfulDeposits.size() + successfulWithdrawals.size())
                .as("at least one operation must succeed for the test to be meaningful")
                .isGreaterThan(0);
        long expectedBalance = initialBalance
                + successfulDeposits.stream().mapToLong(Long::longValue).sum()
                - successfulWithdrawals.stream().mapToLong(Long::longValue).sum();

        assertThat(successfulDeposits).as("at least one deposit must succeed").isNotEmpty();
        Wallet current = walletRepository.findById(wallet.getId()).orElseThrow();
        assertThat(current.getCurrentBalance())
                .isGreaterThanOrEqualTo(0L)
                .isEqualTo(expectedBalance);
    }

    @Test
    void concurrentTransfersConserveMoneyAcrossBothWallets() throws InterruptedException {
        // Given
        long initialBalance = 10_000L;
        seedBalance(initialBalance);
        Wallet destination = freshWallet();

        long transferAmount = 500L;
        List<Long> successfulTransfers = new CopyOnWriteArrayList<>();
        ExecutorService exec = executor();
        ConcurrencyHarness h = harness();

        // When
        for (int i = 0; i < THREAD_COUNT; i++) {
            exec.submit(() -> {
                try {
                    h.ready.countDown();
                    h.start.await();
                    transferHandler.handle(new TransferCommandHandler.Command(
                            UUID.randomUUID().toString(),
                            wallet.getId(),
                            destination.getId(),
                            user.getId(),
                            transferAmount
                    ));
                    successfulTransfers.add(transferAmount);
                } catch (ObjectOptimisticLockingFailureException |
                         PessimisticLockingFailureException _) {
                    // exhausted retries — expected under contention
                } catch (InsufficientBalanceException _) {
                    // valid business rejection — not corruption
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        awaitAndFire(h, exec);

        // Then
        assertThat(successfulTransfers).as("at least one transfer must succeed for the test to be meaningful").isNotEmpty();

        long totalTransferred = successfulTransfers.stream().mapToLong(Long::longValue).sum();
        Wallet currentSource = walletRepository.findById(wallet.getId()).orElseThrow();
        Wallet currentDestination = walletRepository.findById(destination.getId()).orElseThrow();

        assertThat(currentSource.getCurrentBalance()).isGreaterThanOrEqualTo(0L);
        assertThat(currentDestination.getCurrentBalance()).isEqualTo(totalTransferred);
        assertThat(currentSource.getCurrentBalance() + currentDestination.getCurrentBalance())
                .isEqualTo(initialBalance);
    }
}
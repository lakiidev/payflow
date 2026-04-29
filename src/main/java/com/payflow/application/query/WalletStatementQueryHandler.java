package com.payflow.application.query;

import com.payflow.application.dto.TransactionView;
import com.payflow.application.port.WalletStatementPort;
import com.payflow.domain.model.wallet.WalletNotFoundException;
import com.payflow.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class WalletStatementQueryHandler {

    public record Query(UUID userId,UUID walletId, Instant from, Instant to) {}

    private final WalletStatementPort walletStatementPort;
    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public Stream<TransactionView> handle(Query query) {
        assertOwnership(query.walletId(), query.userId());
        return walletStatementPort.findStatementRows(
                query.walletId(),
                query.from(),
                query.to()
        );
    }
    private void assertOwnership(UUID walletId, UUID requestingUserId) {
        var wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        if (!wallet.getUserId().equals(requestingUserId)) {
            throw new WalletNotFoundException(walletId);
        }
    }
}

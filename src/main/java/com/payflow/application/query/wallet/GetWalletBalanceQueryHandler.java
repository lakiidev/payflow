package com.payflow.application.query.wallet;

import com.payflow.api.dto.response.BalanceResponse;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletAccessDeniedException;
import com.payflow.domain.model.wallet.WalletNotFoundException;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetWalletBalanceQueryHandler {

    public record Query(java.util.UUID walletId, java.util.UUID requestingUserId) {}

    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public BalanceResponse handle(Query query) {
        Wallet wallet = walletRepository.findById(query.walletId())
                .orElseThrow(() -> new WalletNotFoundException(query.walletId()));

        if (!wallet.getUserId().equals(query.requestingUserId())) {
            throw new WalletAccessDeniedException(query.walletId());
        }

        return new BalanceResponse(wallet.getId(), wallet.getCurrentBalance(), wallet.getCurrency());
    }
}

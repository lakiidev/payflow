package com.payflow.application.query.walelt;

import com.payflow.api.dto.response.WalletResponse;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletAccessDeniedException;
import com.payflow.domain.model.wallet.WalletNotFoundException;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletQueryHandler {
    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public List<WalletResponse> handle(WalletQuery query) {
        return walletRepository.findAllByUserId(query.userId())
                .stream()
                .map(WalletResponse::from)
                .toList();
    }
    @Transactional(readOnly = true)
    public WalletResponse handle(GetWalletByIdQuery query) {
        Wallet wallet = walletRepository.findById(query.walletId()).orElseThrow(
                () -> new WalletNotFoundException(query.walletId())
        );
        if(!wallet.getUserId().equals(query.requestingUserId())) {
            throw new WalletAccessDeniedException(query.requestingUserId());
        }
        return WalletResponse.from(wallet);
    }
}

package com.payflow.application.query.wallet;

import com.payflow.api.dto.response.WalletResponse;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.model.wallet.WalletNotFoundException;
import com.payflow.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletQueryHandler {

    public record ListQuery(UUID userId) {}
    public record GetByIdQuery(UUID walletId, UUID requestingUserId) {}

    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public List<WalletResponse> handle(ListQuery query) {
        return walletRepository.findAllByUserId(query.userId())
                .stream()
                .map(WalletResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WalletResponse handle(GetByIdQuery query) {
        Wallet wallet = walletRepository.findByIdAndUserId(query.walletId(), query.requestingUserId())
                .orElseThrow(() -> new WalletNotFoundException(query.walletId()));
        return WalletResponse.from(wallet);
    }
}

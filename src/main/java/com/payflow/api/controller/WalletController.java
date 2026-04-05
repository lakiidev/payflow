package com.payflow.api.controller;

import com.payflow.api.dto.response.WalletResponse;
import com.payflow.application.query.walelt.WalletQuery;
import com.payflow.application.query.walelt.WalletQueryHandler;
import com.payflow.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/wallets")
public class WalletController  {
    private final WalletQueryHandler walletQueryHandler;
    @GetMapping
    public List<WalletResponse> getMyWallets(@AuthenticationPrincipal User user) {
        return walletQueryHandler.handle(new WalletQuery(user.getId()));
    }
}

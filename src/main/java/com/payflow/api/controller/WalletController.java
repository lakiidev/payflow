package com.payflow.api.controller;

import com.payflow.api.dto.request.CreateWalletRequest;
import com.payflow.api.dto.response.BalanceResponse;
import com.payflow.api.dto.response.WalletResponse;
import com.payflow.application.command.CreateWalletCommand;
import com.payflow.application.command.CreateWalletCommandHandler;
import com.payflow.application.command.FreezeWalletCommand;
import com.payflow.application.command.FreezeWalletCommandHandler;
import com.payflow.application.query.wallet.GetWalletBalanceQuery;
import com.payflow.application.query.wallet.GetWalletBalanceQueryHandler;
import com.payflow.application.query.wallet.GetWalletByIdQuery;
import com.payflow.application.query.wallet.WalletQuery;
import com.payflow.application.query.wallet.WalletQueryHandler;
import com.payflow.domain.model.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/wallets")
public class WalletController {

    private final WalletQueryHandler walletQueryHandler;
    private final CreateWalletCommandHandler createWalletCommandHandler;
    private final FreezeWalletCommandHandler freezeWalletCommandHandler;
    private final GetWalletBalanceQueryHandler getWalletBalanceQueryHandler;

    @GetMapping
    public List<WalletResponse> getMyWallets(@AuthenticationPrincipal User user) {
        return walletQueryHandler.handle(new WalletQuery(user.getId()));
    }

    @GetMapping("/{id}")
    public WalletResponse getWalletById(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return walletQueryHandler.handle(new GetWalletByIdQuery(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @Valid @RequestBody CreateWalletRequest request,
            @AuthenticationPrincipal User user) {
        WalletResponse response = createWalletCommandHandler.handle(
                new CreateWalletCommand(user.getId(), request.currency()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse getBalance(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return getWalletBalanceQueryHandler.handle(new GetWalletBalanceQuery(id, user.getId()));
    }

    @PostMapping("/{id}/freeze")
    public ResponseEntity<Void> freeze(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        freezeWalletCommandHandler.handle(new FreezeWalletCommand(id, user.getId()));
        return ResponseEntity.noContent().build();
    }
}

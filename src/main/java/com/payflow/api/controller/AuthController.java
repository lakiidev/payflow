package com.payflow.api.controller;

import com.payflow.api.dto.request.LoginRequest;
import com.payflow.api.dto.request.LogoutRequest;
import com.payflow.api.dto.request.RefreshRequest;
import com.payflow.api.dto.request.RegisterRequest;
import com.payflow.api.dto.response.AuthenticationResponse;
import com.payflow.api.dto.response.UserProfileResponse;
import com.payflow.application.command.auth.LogoutCommandHandler;
import com.payflow.application.command.auth.RefreshCommandHandler;
import com.payflow.application.command.auth.RegisterCommandHandler;
import com.payflow.application.command.auth.LoginCommandHandler;
import com.payflow.application.query.CurrentUserQueryHandler;
import com.payflow.domain.model.user.User;
import com.payflow.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterCommandHandler registerCommandHandler;
    private final LoginCommandHandler authenticationQueryHandler;
    private final LogoutCommandHandler logoutCommandHandler;
    private final RefreshCommandHandler refreshCommandHandler;
    private final CurrentUserQueryHandler currentUserQueryHandler;
    private final JwtService jwtService;

    @PostMapping("/register")
    public AuthenticationResponse register(@Valid @RequestBody RegisterRequest request) {
        return registerCommandHandler.handle(new RegisterCommandHandler.Command(
                request.getEmail(),
                request.getPassword(),
                request.getFullName()
        ));
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationQueryHandler.handle(new LoginCommandHandler.Command(
                request.getEmail(),
                request.getPassword()
        ));
    }

    @PostMapping("/refresh")
    public AuthenticationResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return refreshCommandHandler.handle(new RefreshCommandHandler.Command(
                request.getRefreshToken()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(currentUserQueryHandler.handle(
                new CurrentUserQueryHandler.Query(user.getId())));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LogoutRequest request
    ) {
        String accessToken = jwtService.extractBearerToken(authHeader);
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }

        String jti = jwtService.extractJti(accessToken);
        java.util.Date expiration = jwtService.extractExpiration(accessToken);
        if (jti == null || expiration == null) {
            return ResponseEntity.badRequest().build();
        }

        long ttlSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        logoutCommandHandler.handle(new LogoutCommandHandler.Command(
                jti,
                Math.max(ttlSeconds, 0),
                request.refreshToken()
        ));
        return ResponseEntity.noContent().build();
    }
}

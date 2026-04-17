package com.payflow.api.controller;

import com.payflow.api.dto.request.LoginRequest;
import com.payflow.api.dto.request.RefreshRequest;
import com.payflow.api.dto.request.RegisterRequest;
import com.payflow.api.dto.response.AuthenticationResponse;
import com.payflow.api.dto.response.UserProfileResponse;
import com.payflow.application.command.LogoutCommandHandler;
import com.payflow.application.command.RefreshCommandHandler;
import com.payflow.application.command.RegisterCommandHandler;
import com.payflow.application.command.LoginCommandHandler;
import com.payflow.application.query.GetCurrentUserQueryHandler;
import com.payflow.domain.model.user.User;
import com.payflow.infrastructure.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final GetCurrentUserQueryHandler getCurrentUserQueryHandler;
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
        return ResponseEntity.ok(getCurrentUserQueryHandler.handle(
                new GetCurrentUserQueryHandler.Query(user.getId())));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user, HttpServletRequest request) {
        // Week 3: replace with jwtService.extractJti(token) for Redis denylist key
        String token = jwtService.extractBearerToken(request.getHeader("Authorization"));
        logoutCommandHandler.handle(new LogoutCommandHandler.Command(user.getId(), token));
        return ResponseEntity.noContent().build();
    }
}

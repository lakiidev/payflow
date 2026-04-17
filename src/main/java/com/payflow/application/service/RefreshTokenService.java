package com.payflow.application.service;

import com.payflow.domain.model.token.InvalidRefreshTokenException;
import com.payflow.domain.model.token.RefreshToken;
import com.payflow.infrastructure.persistence.jpa.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class RefreshTokenService {
    @Value("${app.jwt.refresh-expiration}")
    private final long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public String issue(UUID userId) {
        String token = generateSecureToken();

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(userId)
                .tokenHash(
                        sha256Hex(token)
                )
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .createdAt(Instant.now())
                .build());
        return token;
    }

    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(sha256Hex(rawToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }
    public RefreshToken validate(String rawToken) {
        String hash = sha256Hex(rawToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Token not found"));
        if (token.isRevoked()) {
            revokeAllByUserId(token.getUserId());
            throw new InvalidRefreshTokenException("Token already revoked");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Token expired");
        }
        return token;
    }
    public void revokeAllByUserId(UUID userId) {
        refreshTokenRepository.findAllByUserId(userId)
                .forEach(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }


    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

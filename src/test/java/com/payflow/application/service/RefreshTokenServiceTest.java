package com.payflow.application.service;


import com.payflow.domain.model.token.InvalidRefreshTokenException;
import com.payflow.domain.model.token.RefreshToken;
import com.payflow.domain.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;


    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(1000L,refreshTokenRepository);
    }
    private RefreshToken validToken(UUID userId) {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenHash("hashed")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
    }

    @Test
    void shouldThrowWhenTokenNotFound() {
        // Given
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());
        // When + Then
        assertThatThrownBy(() -> refreshTokenService.validate("raw-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldThrowAndRevokeAllOnRevokedToken() {
        // Given
        UUID userId = UUID.randomUUID();
        RefreshToken token1 = validToken(userId);
        RefreshToken token2 = validToken(userId);

        token1.revoke(); // already revoked — should be skipped

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token1));
        when(refreshTokenRepository.findAllByUserId(userId)).thenReturn(List.of(token1, token2));

        // Then
        assertThatThrownBy(() -> refreshTokenService.validate("raw-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, times(1)).save(argThat(RefreshToken::isRevoked));
        verify(refreshTokenRepository, never()).save(token1); // already revoked, not persisted again
    }


    @Test
    void shouldThrowWhenTokenExpired() {
        // Given
        RefreshToken expired = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .tokenHash("hash")
                .expiresAt(Instant.now().minusSeconds(1))
                .revoked(false)
                .build();
        //when
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(expired));

        // Then
        assertThatThrownBy(() -> refreshTokenService.validate("raw-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
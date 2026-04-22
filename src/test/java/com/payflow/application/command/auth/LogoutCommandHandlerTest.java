package com.payflow.application.command.auth;

import com.payflow.application.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;


import java.time.Duration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutCommandHandlerTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    @Mock RefreshTokenService refreshTokenService;

    @InjectMocks LogoutCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void shouldWriteJtiToDenylistOnLogout() {
        // Given
        var command = new LogoutCommandHandler.Command("test-jti-123", 300L, "raw-refresh-token");

        // When
        handler.handle(command);

        // Then
        verify(valueOps).set("denylist:test-jti-123", "1", Duration.ofSeconds(300));
    }

    @Test
    void shouldRevokeRefreshTokenOnLogout() {
        // Given
        var command = new LogoutCommandHandler.Command("test-jti-123", 300L, "some-raw-token");

        // When
        handler.handle(command);

        // Then
        verify(refreshTokenService).revoke("some-raw-token");
    }
}

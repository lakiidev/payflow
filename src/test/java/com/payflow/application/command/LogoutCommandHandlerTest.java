package com.payflow.application.command;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

class LogoutCommandHandlerTest {

    private final LogoutCommandHandler handler = new LogoutCommandHandler();

    @ParameterizedTest(name = "token={0}")
    @NullSource
    @ValueSource(strings = {"some.jwt.token"})
    void handleCompletesForAnyToken(String token) {
        // Week 1: no-op — jti unused until Week 3 Redis denylist
        assertThatCode(() -> handler.handle(
                new LogoutCommandHandler.Command(UUID.randomUUID(), token)))
                .doesNotThrowAnyException();
    }
}

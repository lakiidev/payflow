package com.payflow.application.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class LogoutCommandHandlerTest {

    @InjectMocks
    private LogoutCommandHandler handler;

    @Test
    void shouldCompleteWithoutErrorForValidCommand() {
        LogoutCommand command = new LogoutCommand(UUID.randomUUID(), "some.jwt.token");

        assertThatCode(() -> handler.handle(command)).doesNotThrowAnyException();
    }

    @Test
    void shouldCompleteWithoutErrorWhenTokenJtiIsNull() {
        // Valid for Week 1 — jti is unused until Week 3 Redis denylist
        LogoutCommand command = new LogoutCommand(UUID.randomUUID(), null);

        assertThatCode(() -> handler.handle(command)).doesNotThrowAnyException();
    }
}

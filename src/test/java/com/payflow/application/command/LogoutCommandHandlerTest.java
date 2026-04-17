package com.payflow.application.command;

import com.payflow.application.command.auth.LogoutCommandHandler;
import com.payflow.application.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutCommandHandlerTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private LogoutCommandHandler handler;

    @Test
    void shouldRevokeRefreshTokenOnLogout() {
        String rawToken = "some-raw-token";

        handler.handle(new LogoutCommandHandler.Command(rawToken));

        verify(refreshTokenService).revoke(rawToken);
    }
}

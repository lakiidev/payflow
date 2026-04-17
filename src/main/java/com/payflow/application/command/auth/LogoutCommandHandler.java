package com.payflow.application.command.auth;

import com.payflow.application.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class LogoutCommandHandler {

    // Week 1: token string
    // Week 3: tokenJti
    public record Command(String token) {}
    private final RefreshTokenService refreshTokenService;
    // Week 1: no-op, client discards token
    // Week 3: redisTemplate.opsForValue().set("denylist:" + command.tokenJti(), ...)
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void handle(Command command) {
        refreshTokenService.revoke(
                command.token()
        );
    }
}

package com.payflow.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutCommandHandler {

    public record Command(java.util.UUID userId, String tokenJti) {}

    // Week 1: no-op, client discards token
    // Week 3: redisTemplate.opsForValue().set("denylist:" + command.tokenJti(), ...)
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void handle(Command command) {
        // No-op
    }
}

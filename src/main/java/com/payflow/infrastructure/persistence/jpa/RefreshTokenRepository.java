package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.token.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String token);
    List<RefreshToken> findAllByUserId(UUID userId);
    void deleteAllByUserId(UUID userId);
}
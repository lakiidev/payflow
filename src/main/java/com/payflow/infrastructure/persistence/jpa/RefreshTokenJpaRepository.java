package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.token.RefreshToken;
import com.payflow.domain.repository.RefreshTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, UUID>, RefreshTokenRepository {

}
package com.payflow.infrastructure.persistence.jpa;

import com.payflow.domain.model.user.User;
import com.payflow.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<User, UUID>, UserRepository {

    Optional<User> findById(UUID userId);
}

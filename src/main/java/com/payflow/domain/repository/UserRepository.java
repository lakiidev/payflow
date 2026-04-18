package com.payflow.domain.repository;

import com.payflow.domain.model.user.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findById(UUID uuid);
    User save(User user);
}
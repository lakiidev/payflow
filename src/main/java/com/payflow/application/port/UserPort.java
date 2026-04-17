package com.payflow.application.port;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface UserPort {
    UserDetails loadByEmail(String email);
    UserDetails loadById(UUID userId);
}
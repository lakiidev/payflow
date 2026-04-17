package com.payflow.application.command.auth;


import com.payflow.TestcontainersConfiguration;
import com.payflow.api.dto.request.LoginRequest;
import com.payflow.api.dto.request.RegisterRequest;
import com.payflow.infrastructure.persistence.jpa.UserRepository;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@AutoConfigureRestTestClient
class LoginIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;

    private String userEmail;

    @BeforeEach
    void setUp() {
        userEmail = "login-" + UUID.randomUUID() + "@payflow.com";
        restTestClient.post()
                .uri("/api/v1/auth/register")
                .body(RegisterRequest.builder()
                        .email(userEmail)
                        .password("password123")
                        .fullName("Test User")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }


    @Test
    void shouldLoginUserAndReturnTokens() {

        // When
        restTestClient.post()
                .uri("/api/v1/auth/login")
                .body(LoginRequest.builder()
                        .email(userEmail)
                        .password("password123")
                        .build()
                )
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldReturn401WithWrongPassword() {

        restTestClient.post()
                .uri("/api/v1/auth/login")
                .body(LoginRequest.builder()
                        .email(userEmail)
                        .password("wrongpassword")
                        .build())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn401WithWrongEmail() {

        restTestClient.post()
                .uri("/api/v1/auth/login")
                .body(LoginRequest.builder()
                        .email("nonexistent@payflow.com")
                        .password("password123")
                        .build())
                .exchange()
                .expectStatus().isUnauthorized();
    }

}

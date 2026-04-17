package com.payflow.application.command.auth;


import com.payflow.TestcontainersConfiguration;
import com.payflow.api.dto.request.LoginRequest;
import com.payflow.api.dto.request.RegisterRequest;
import com.payflow.infrastructure.persistence.jpa.UserRepository;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@AutoConfigureRestTestClient
class LoginIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;

    private void registerUser(String email) {
        restTestClient.post()
                .uri("/api/v1/auth/register")
                .body(RegisterRequest.builder()
                        .email(email)
                        .password("password123")
                        .fullName("Test User")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldLoginUserAndReturnTokens() {
        // Setup
        registerUser("login-happy@payflow.com");

        // When
        restTestClient.post()
                .uri("/api/v1/auth/login")
                .body(LoginRequest.builder()
                        .email("login-happy@payflow.com")
                        .password("password123")
                        .build()
                )
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldReturn401WithWrongPassword() {
        registerUser("login-wrong@payflow.com");

        restTestClient.post()
                .uri("/api/v1/auth/login")
                .body(LoginRequest.builder()
                        .email("login-wrong@payflow.com")
                        .password("wrongpassword")
                        .build())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn401WithWrongEmail() {
        registerUser("login-wrong@payflow.com");

        restTestClient.post()
                .uri("/api/v1/auth/login")
                .body(LoginRequest.builder()
                        .email("login-wrong-mail@payflow.com")
                        .password("password123")
                        .build())
                .exchange()
                .expectStatus().isUnauthorized();
    }

}

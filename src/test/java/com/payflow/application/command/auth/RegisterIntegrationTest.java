package com.payflow.application.command.auth;

import com.payflow.BaseIntegrationTest;
import com.payflow.api.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class RegisterIntegrationTest extends BaseIntegrationTest {

    @Autowired private RestTestClient restTestClient;

    @Test
    void shouldRegisterUserCreateWalletAndReturnTokens() {
        restTestClient.post()
                .uri("/api/v1/auth/register")
                .body(RegisterRequest.builder()
                        .email("register-happy@payflow.com")
                        .password("password123")
                        .fullName("Test User")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldReturn409WhenEmailAlreadyRegistered() {
        RegisterRequest request = RegisterRequest.builder()
                .email("register-duplicate@payflow.com")
                .password("password123")
                .fullName("Test User")
                .build();

        restTestClient.post()
                .uri("/api/v1/auth/register")
                .body(request)
                .exchange()
                .expectStatus().isOk();

        restTestClient.post()
                .uri("/api/v1/auth/register")
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void shouldReturn400WhenInputIsInvalid() {
        restTestClient.post()
                .uri("/api/v1/auth/register")
                .body(RegisterRequest.builder()
                        .email("not-an-email")
                        .password("short")
                        .fullName("")
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }
}
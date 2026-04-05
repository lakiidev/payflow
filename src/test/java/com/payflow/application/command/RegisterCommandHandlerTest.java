package com.payflow.application.command;

import com.payflow.api.dto.response.AuthenticationResponse;
import com.payflow.domain.model.user.User;
import com.payflow.infrastructure.persistence.jpa.UserRepository;
import com.payflow.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RegisterCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RegisterCommandHandler handler;

    @Test
    void shouldRegisterUserAndReturnTokens() {
        // Given
        RegisterCommand command = new RegisterCommand(
                "test@payflow.com",
                "password123",
                "Test User"
        );
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        // When
        AuthenticationResponse response = handler.handle(command);

        // Then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getEmail()).isEqualTo("test@payflow.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void shouldThrowWhenEmailAlreadyRegistered() {
        // Given

        RegisterCommand command = new RegisterCommand(
                    "existing@payflow.com",
                "password123",
                "Existing User"
        );
        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }
}
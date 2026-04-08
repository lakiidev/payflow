package com.payflow.application.query;

import com.payflow.api.dto.response.AuthenticationResponse;
import com.payflow.domain.model.user.User;
import com.payflow.domain.model.user.UserStatus;
import com.payflow.infrastructure.persistence.jpa.UserRepository;
import com.payflow.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthQueryHandlerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthQueryHandler handler;

    @Test
    void shouldLoginAndReturnTokens() {
        // Given
        AuthQueryHandler.Query query = new AuthQueryHandler.Query("test@payflow.com", "password123");

        User user = User.builder()
                .email("test@payflow.com")
                .passwordHash("hashed")
                .fullName("Test User")
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail(query.email())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        // When
        AuthenticationResponse response = handler.handle(query);

        // Then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(authenticationManager).authenticate(any());
    }
    @Test
    void shouldRefreshAndReturnNewTokens() {
        // Given
        String oldRefreshToken = "old-refresh-token";
        String email = "test@payflow.com";

        User user = User.builder()
                .email(email)
                .passwordHash("hashed")
                .fullName("Test User")
                .status(UserStatus.ACTIVE)
                .build();

        when(jwtService.extractUsername(oldRefreshToken)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(user);
        when(jwtService.validateToken(oldRefreshToken, user)).thenReturn(true);
        when(jwtService.generateAccessToken(any())).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("new-refresh-token");

        // When
        AuthenticationResponse response = handler.handleRefresh(oldRefreshToken);

        // Then
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.getEmail()).isEqualTo(email);
    }

    @Test
    void shouldThrowWhenRefreshTokenInvalid() {
        // Given
        String invalidToken = "invalid-token";

        when(jwtService.extractUsername(invalidToken)).thenReturn(null);

        // When + Then
        assertThrows(BadCredentialsException.class,
                () -> handler.handleRefresh(invalidToken));
    }

    @Test
    void shouldThrowWhenRefreshTokenExpired() {
        // Given
        String expiredToken = "expired-refresh-token";
        String email = "test@payflow.com";
        User user = User.builder()
                .email(email)
                .passwordHash("hashed")
                .fullName("Test User")
                .status(UserStatus.ACTIVE)
                .build();

        when(jwtService.extractUsername(expiredToken)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(user);
        when(jwtService.validateToken(expiredToken, user)).thenReturn(false);

        // When + Then
        assertThatThrownBy(() -> handler.handleRefresh(expiredToken))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldThrowWhenUserNotFoundDuringLogin() {
        // Given
        AuthQueryHandler.Query query = new AuthQueryHandler.Query("unknown@payflow.com", "password123");

        when(userRepository.findByEmail(query.email())).thenReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
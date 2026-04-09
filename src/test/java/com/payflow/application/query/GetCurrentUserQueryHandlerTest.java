package com.payflow.application.query;

import com.payflow.api.dto.response.UserProfileResponse;
import com.payflow.domain.model.user.User;
import com.payflow.infrastructure.persistence.jpa.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserQueryHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetCurrentUserQueryHandler handler;

    @Test
    void shouldReturnProfileForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .email("user@payflow.com")
                .fullName("Test User")
                .passwordHash("hashed")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileResponse response = handler.handle(new GetCurrentUserQueryHandler.Query(userId));

        assertThat(response.email()).isEqualTo("user@payflow.com");
        assertThat(response.fullName()).isEqualTo("Test User");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetCurrentUserQueryHandler.Query(userId)))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}

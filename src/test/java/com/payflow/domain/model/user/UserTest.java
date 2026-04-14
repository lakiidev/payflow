package com.payflow.domain.model.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private User user(UserStatus status) {
        return User.builder()
                .email("test@payflow.com")
                .passwordHash("hashed")
                .fullName("Test User")
                .status(status)
                .build();
    }

    @Test
    void shouldReturnEmailAsUsername() {
        assertThat(user(UserStatus.ACTIVE).getUsername()).isEqualTo("test@payflow.com");
    }


    @Test
    void shouldBeEnabledOnlyWhenActive() {
        assertThat(user(UserStatus.ACTIVE).isEnabled()).isTrue();
        assertThat(user(UserStatus.SUSPENDED).isEnabled()).isFalse();
        assertThat(user(UserStatus.CLOSED).isEnabled()).isFalse();
    }

    @Test
    void shouldBeLockedWhenSuspended() {
        assertThat(user(UserStatus.SUSPENDED).isAccountNonLocked()).isFalse();
        assertThat(user(UserStatus.ACTIVE).isAccountNonLocked()).isTrue();
        assertThat(user(UserStatus.CLOSED).isAccountNonLocked()).isTrue();
    }
}

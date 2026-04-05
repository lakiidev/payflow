package com.payflow.application.command;

import com.payflow.api.dto.response.AuthenticationResponse;
import com.payflow.domain.model.user.User;
import com.payflow.domain.model.user.UserStatus;
import com.payflow.infrastructure.persistence.jpa.UserRepository;
import com.payflow.infrastructure.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterCommandHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional // For future use, convention wise atm
    public AuthenticationResponse handle(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new BadCredentialsException(command.email());
        }
        User user = User.builder()
                .fullName(command.fullName())
                .status(UserStatus.ACTIVE)
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .build();
        userRepository.save(user);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthenticationResponse.builder()
                .email(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}

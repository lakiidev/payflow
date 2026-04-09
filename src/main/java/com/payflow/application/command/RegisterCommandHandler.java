// TODO(Week 3): Move to LoginCommandHandler when stateful refresh tokens
// introduce real DB writes — currently stateless so side effects are minimal.

package com.payflow.application.command;

import com.payflow.api.dto.response.AuthenticationResponse;
import com.payflow.domain.model.user.EmailAlreadyRegisteredException;
import com.payflow.domain.model.user.User;
import com.payflow.domain.model.user.UserStatus;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.infrastructure.persistence.jpa.UserRepository;
import com.payflow.infrastructure.persistence.jpa.WalletRepository;
import com.payflow.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

@Service
@RequiredArgsConstructor
public class RegisterCommandHandler {

    public record Command(String email, String password, String fullName) {}

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional // For future use, convention wise atm
    public AuthenticationResponse handle(Command command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyRegisteredException(command.email());
        }
        User user = User.builder()
                .fullName(command.fullName())
                .status(UserStatus.ACTIVE)
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .build();
        userRepository.save(user);

        Wallet wallet = Wallet.create(user.getId(), Currency.getInstance("GBP"));
        walletRepository.save(wallet);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthenticationResponse.builder()
                .email(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}

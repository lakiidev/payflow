// introduce real DB writes — currently stateless so side effects are minimal.

package com.payflow.application.command.auth;

import com.payflow.api.dto.response.AuthenticationResponse;
import com.payflow.application.port.TokenPort;
import com.payflow.application.service.RefreshTokenService;
import com.payflow.domain.model.user.EmailAlreadyRegisteredException;
import com.payflow.domain.model.user.User;
import com.payflow.domain.model.user.UserStatus;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.domain.repository.RefreshTokenRepository;
import com.payflow.domain.repository.UserRepository;
import com.payflow.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

@Service
@RequiredArgsConstructor
public class RegisterCommandHandler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    public record Command(String email, String password, String fullName) {}

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenPort tokenPort;

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
        String accessToken = tokenPort.generateAccessToken(user);
        String rawRefreshToken = refreshTokenService.issue(user.getId());
        return AuthenticationResponse.builder()
                .email(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .build();
    }
}

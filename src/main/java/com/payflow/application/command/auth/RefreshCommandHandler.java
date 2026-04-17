package com.payflow.application.command.auth;

import com.payflow.api.dto.response.AuthenticationResponse;
import com.payflow.application.port.TokenPort;
import com.payflow.application.port.UserPort;
import com.payflow.application.service.RefreshTokenService;
import com.payflow.domain.model.token.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshCommandHandler {
    private final AuthenticationManager authenticationManager;
    private final TokenPort tokenPort;
    private final UserPort userPort;
    private final RefreshTokenService refreshTokenService;
    public record Command(String rawRefreshToken) {}
    public AuthenticationResponse handle(Command command)
    {
        RefreshToken token = refreshTokenService.validate(command.rawRefreshToken());
        refreshTokenService.revoke(command.rawRefreshToken());

        UserDetails userDetails = userPort.loadById(token.getUserId());

        String newAccessToken = tokenPort.generateAccessToken(userDetails);
        String newRefreshToken = refreshTokenService.issue(token.getUserId());
        return AuthenticationResponse
                .builder()
                .email(userDetails.getUsername())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}

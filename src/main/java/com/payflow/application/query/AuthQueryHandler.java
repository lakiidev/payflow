package com.payflow.application.query;


import com.payflow.api.dto.response.AuthenticationResponse;
import com.payflow.domain.model.user.User;
import com.payflow.infrastructure.persistence.jpa.UserRepository;
import com.payflow.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthQueryHandler {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Transactional(readOnly = true)
    public AuthenticationResponse handle(AuthQuery query)
    {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                query.email(),
                query.password()
        ));
        User user = userRepository.findByEmail(query.email()).orElseThrow(()->
                new UsernameNotFoundException("User not found: "+ query.email()));
        String accessToken= jwtService.generateAccessToken(user);
        String refreshToken= jwtService.generateRefreshToken(user);
        return AuthenticationResponse.builder()
                .email(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse handleRefresh(String refreshToken)
    {
        String email = jwtService.extractUsername(refreshToken);
        if(email==null)
        {
            throw new BadCredentialsException("Invalid refresh token");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!jwtService.validateToken(refreshToken, userDetails)) {
            throw new BadCredentialsException("Refresh token expired or invalid");
        }
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);
        return AuthenticationResponse.builder().email(userDetails.getUsername()).accessToken(newAccessToken).refreshToken(newRefreshToken).build();
    }
}

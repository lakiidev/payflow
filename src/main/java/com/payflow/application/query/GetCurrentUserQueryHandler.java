package com.payflow.application.query;

import com.payflow.api.dto.response.UserProfileResponse;
import com.payflow.infrastructure.persistence.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCurrentUserQueryHandler {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse handle(GetCurrentUserQuery query) {
        return userRepository.findById(query.userId())
                .map(u -> new UserProfileResponse(u.getId(), u.getUsername(), u.getFullName()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + query.userId()));
    }
}

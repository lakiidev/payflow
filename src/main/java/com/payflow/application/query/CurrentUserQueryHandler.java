package com.payflow.application.query;

import com.payflow.api.dto.response.UserProfileResponse;
import com.payflow.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserQueryHandler {

    public record Query(java.util.UUID userId) {}

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse handle(Query query) {
        return userRepository.findById(query.userId())
                .map(u -> new UserProfileResponse(u.getId(), u.getUsername(), u.getFullName()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + query.userId()));
    }
}

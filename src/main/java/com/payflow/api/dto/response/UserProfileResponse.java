package com.payflow.api.dto.response;

import java.util.UUID;

public record UserProfileResponse(UUID id, String email, String fullName) {}

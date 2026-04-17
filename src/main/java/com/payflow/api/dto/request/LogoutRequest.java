package com.payflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(@NotBlank String refreshToken){
}

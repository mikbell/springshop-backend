package com.michelecampanello.springshop.domains.users.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Il refresh token è obbligatorio")
        String refreshToken
) {}

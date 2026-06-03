package com.michelecampanello.springshop.domains.users.dto;

import java.util.UUID;

public record AuthenticationResponse(
        String token,
        String refreshToken,
        UUID userId,
        String email
) {}
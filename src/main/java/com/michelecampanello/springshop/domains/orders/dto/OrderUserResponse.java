package com.michelecampanello.springshop.domains.orders.dto;

import com.michelecampanello.springshop.domains.users.model.User;

import java.util.UUID;

public record OrderUserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        User.Role role,
        Boolean active
) {
}

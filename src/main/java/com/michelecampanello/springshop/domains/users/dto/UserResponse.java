package com.michelecampanello.springshop.domains.users.dto;

import com.michelecampanello.springshop.domains.addresses.dto.AddressResponse;
import com.michelecampanello.springshop.domains.users.model.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        AddressResponse address,
        User.Role role,
        Boolean active
) {
}

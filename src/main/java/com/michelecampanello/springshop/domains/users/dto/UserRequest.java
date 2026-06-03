package com.michelecampanello.springshop.domains.users.dto;

import com.michelecampanello.springshop.domains.addresses.dto.AddressRequest;
import com.michelecampanello.springshop.domains.users.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Inserisci un indirizzo email valido") String email,
        @NotBlank(message = "La password è obbligatoria")
        @Size(min = 6, message = "La password deve contenere almeno 6 caratteri") String password,
        String phoneNumber,
        @Valid AddressRequest address,
        User.Role role
) {
}

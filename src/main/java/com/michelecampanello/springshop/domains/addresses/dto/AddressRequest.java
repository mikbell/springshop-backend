package com.michelecampanello.springshop.domains.addresses.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String street,
        @NotBlank String city,
        String state,
        @NotBlank String country,
        @NotBlank String zipcode
) {
}

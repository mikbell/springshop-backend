package com.michelecampanello.springshop.domains.addresses.dto;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String street,
        String city,
        String state,
        String country,
        String zipcode
) {
}

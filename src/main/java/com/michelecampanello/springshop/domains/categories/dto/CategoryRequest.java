package com.michelecampanello.springshop.domains.categories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Il nome della categoria e obbligatorio")
        @Size(min = 2, max = 100, message = "Il nome deve essere compreso tra 2 e 100 caratteri")
        String name,
        String description
) {
}

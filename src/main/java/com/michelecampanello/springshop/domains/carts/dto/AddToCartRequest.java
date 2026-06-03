package com.michelecampanello.springshop.domains.carts.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddToCartRequest(
        @NotNull(message = "L'ID del prodotto è obbligatorio")
        UUID productId,

        @NotNull(message = "La quantità è obbligatorio")
        @Min(value = 1, message = "La quantità deve essere almeno 1")
        Integer quantity
) {}
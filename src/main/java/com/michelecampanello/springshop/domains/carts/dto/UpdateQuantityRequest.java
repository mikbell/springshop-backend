package com.michelecampanello.springshop.domains.carts.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateQuantityRequest(
        @NotNull(message = "La quantità è obbligatoria")
        @Min(value = 0, message = "La quantità non può essere negativa") // 👈 Permettiamo lo 0 per la rimozione automatica
        Integer quantity
) {}
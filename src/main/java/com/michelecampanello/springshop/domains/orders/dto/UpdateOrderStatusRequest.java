package com.michelecampanello.springshop.domains.orders.dto;

import com.michelecampanello.springshop.domains.orders.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Lo stato è obbligatorio")
        OrderStatus status
) {}

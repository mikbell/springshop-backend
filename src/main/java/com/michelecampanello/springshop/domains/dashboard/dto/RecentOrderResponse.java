package com.michelecampanello.springshop.domains.dashboard.dto;

import com.michelecampanello.springshop.domains.orders.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecentOrderResponse(
        UUID id,
        String orderNumber,
        UUID userId,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
}

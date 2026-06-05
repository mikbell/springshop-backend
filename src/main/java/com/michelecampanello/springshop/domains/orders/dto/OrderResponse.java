package com.michelecampanello.springshop.domains.orders.dto;

import com.michelecampanello.springshop.domains.orders.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        UUID userId,
        OrderUserResponse user,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
        public OrderResponse(
                UUID id,
                String orderNumber,
                UUID userId,
                OrderStatus status,
                BigDecimal totalAmount,
                List<OrderItemResponse> items,
                LocalDateTime createdAt
        ) {
                this(id, orderNumber, userId, null, status, totalAmount, items, createdAt);
        }
}

package com.michelecampanello.springshop.domains.orders.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        String productName,
        String sku,
        OrderProductResponse product,
        BigDecimal priceAtPurchase,
        Integer quantity,
        BigDecimal totalPrice
) {
        public OrderItemResponse(
                UUID id,
                UUID productId,
                String productName,
                String sku,
                BigDecimal priceAtPurchase,
                Integer quantity,
                BigDecimal totalPrice
        ) {
                this(id, productId, productName, sku, null, priceAtPurchase, quantity, totalPrice);
        }
}

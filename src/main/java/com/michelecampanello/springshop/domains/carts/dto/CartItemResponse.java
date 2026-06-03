package com.michelecampanello.springshop.domains.carts.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        String sku,
        Integer quantity,
        BigDecimal priceAtAdded,
        BigDecimal totalPrice
) {}
package com.michelecampanello.springshop.domains.orders.dto;

import com.michelecampanello.springshop.domains.products.model.Product;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderProductResponse(
        UUID id,
        String name,
        String sku,
        String slug,
        String imageUrl,
        BigDecimal currentPrice,
        Integer currentStockQuantity,
        Product.ProductStatus status
) {
}

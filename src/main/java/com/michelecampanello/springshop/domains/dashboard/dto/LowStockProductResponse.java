package com.michelecampanello.springshop.domains.dashboard.dto;

import com.michelecampanello.springshop.domains.products.model.Product;

import java.util.UUID;

public record LowStockProductResponse(
        UUID id,
        String name,
        String sku,
        int stockQuantity,
        Product.ProductStatus status
) {
}

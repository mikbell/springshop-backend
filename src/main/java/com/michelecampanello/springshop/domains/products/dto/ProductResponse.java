package com.michelecampanello.springshop.domains.products.dto;

import com.michelecampanello.springshop.domains.categories.dto.CategorySummaryResponse;
import com.michelecampanello.springshop.domains.products.model.Product.ProductStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String sku,
        String slug,
        String imageUrl,
        CategorySummaryResponse category,
        ProductStatus status,
        Double averageRating,
        Long reviewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {
        public ProductResponse(UUID id, String name, String description, BigDecimal price, Integer stockQuantity,
                               String sku, String slug, String imageUrl, ProductStatus status,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
                this(id, name, description, price, stockQuantity, sku, slug, imageUrl, null, status, null, 0L, createdAt, updatedAt);
        }

        public ProductResponse(UUID id, String name, String description, BigDecimal price, Integer stockQuantity,
                               String sku, String slug, String imageUrl, CategorySummaryResponse category,
                               ProductStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
                this(id, name, description, price, stockQuantity, sku, slug, imageUrl, category, status, null, 0L, createdAt, updatedAt);
        }
}

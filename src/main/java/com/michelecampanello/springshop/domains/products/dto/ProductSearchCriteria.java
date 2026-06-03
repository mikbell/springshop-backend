package com.michelecampanello.springshop.domains.products.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSearchCriteria(
        String searchTerm,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean onlyAvailable,
        UUID categoryId,
        String categorySlug
) {
        public ProductSearchCriteria(String searchTerm, BigDecimal minPrice, BigDecimal maxPrice, Boolean onlyAvailable) {
                this(searchTerm, minPrice, maxPrice, onlyAvailable, null, null);
        }
}

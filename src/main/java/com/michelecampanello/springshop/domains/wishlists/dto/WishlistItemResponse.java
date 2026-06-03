package com.michelecampanello.springshop.domains.wishlists.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WishlistItemResponse(
        UUID id,
        UUID productId,
        String productName,
        String sku,
        BigDecimal price,
        String imageUrl,
        LocalDateTime addedAt
) {
}

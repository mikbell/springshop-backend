package com.michelecampanello.springshop.domains.categories.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {
}

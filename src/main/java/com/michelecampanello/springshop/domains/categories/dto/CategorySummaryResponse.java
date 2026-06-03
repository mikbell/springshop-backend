package com.michelecampanello.springshop.domains.categories.dto;

import java.io.Serializable;
import java.util.UUID;

public record CategorySummaryResponse(
        UUID id,
        String name,
        String slug
) implements Serializable {
}

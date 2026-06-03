package com.michelecampanello.springshop.domains.products.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
        @NotBlank(message = "Il nome del prodotto è obbligatorio")
        @Size(min = 2, max = 150, message = "Il nome deve essere compreso tra 2 e 150 caratteri")
        String name,
        String description,

        @NotNull(message = "Il prezzo è obbligatorio")
        @DecimalMin(value = "0.01", message = "Il prezzo deve essere maggiore di zero")
        BigDecimal price,

        @NotNull(message = "La quantità in magazzino è obbligatoria")
        @Min(value = 0, message = "Lo stock non può essere negativo")
        Integer stockQuantity,

        @NotBlank(message = "Il codice SKU è obbligatorio")
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "Lo SKU può contenere solo lettere maiuscole, numeri e trattini")
        String sku,

        String imageUrl,

        UUID categoryId
) {
        public ProductRequest(String name, String description, BigDecimal price, Integer stockQuantity, String sku, String imageUrl) {
                this(name, description, price, stockQuantity, sku, imageUrl, null);
        }
}

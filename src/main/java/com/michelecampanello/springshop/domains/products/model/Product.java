package com.michelecampanello.springshop.domains.products.model;

import com.michelecampanello.springshop.core.model.BaseEntity; // Importa la classe base
import com.michelecampanello.springshop.domains.categories.model.Category;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(unique = true, nullable = false, length = 50, updatable = false)
    private String sku;

    @Column(unique = true, nullable = false, length = 170, updatable = false)
    private String slug;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;

    @PrePersist
    @PreUpdate
    protected void checkStockStatus() {
        if (stockQuantity != null && stockQuantity <= 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (stockQuantity != null && this.status == ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.AVAILABLE;
        }
    }

    public enum ProductStatus {
        AVAILABLE, OUT_OF_STOCK, DISCONTINUED
    }
}

package com.michelecampanello.springshop.domains.carts.model;

import com.michelecampanello.springshop.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Getter @Setter
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // Scollegato da Product entità: memorizziamo solo l'ID e lo SKU per i controlli
    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal priceAtAdded; // Il prezzo del prodotto quando è stato messo nel carrello

    // Calcolo dinamico del prezzo di questa riga
    public BigDecimal getTotalPrice() {
        return priceAtAdded.multiply(BigDecimal.valueOf(quantity));
    }
}
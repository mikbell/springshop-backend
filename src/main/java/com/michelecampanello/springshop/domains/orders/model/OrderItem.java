package com.michelecampanello.springshop.domains.orders.model;

import com.michelecampanello.springshop.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private UUID productId; // Riferimento logico al prodotto

    @Column(nullable = false)
    private String productName; // Snapshot del nome per lo storico

    @Column(nullable = false)
    private String sku; // Snapshot dello SKU

    @Column(nullable = false)
    private BigDecimal priceAtPurchase; // Prezzo congelato al momento dell'acquisto

    @Column(nullable = false)
    private Integer quantity;

    public BigDecimal getRowTotal() {
        return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }
}
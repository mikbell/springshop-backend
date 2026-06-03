package com.michelecampanello.springshop.domains.carts.model;

import com.michelecampanello.springshop.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Getter
@Setter
public class Cart extends BaseEntity {

    // Scollegato da User entità: salviamo solo il riferimento logico
    @Column(nullable = false, unique = true)
    private UUID userId;

    // Relazione uno-a-molti con le righe del carrello
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    // Metodo helper per calcolare il totale del carrello al volo
    public java.math.BigDecimal getTotalCartPrice() {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
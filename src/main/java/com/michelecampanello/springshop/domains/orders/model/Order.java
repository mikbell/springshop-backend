package com.michelecampanello.springshop.domains.orders.model;

import com.michelecampanello.springshop.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order extends BaseEntity {

    @Column(nullable = false)
    private UUID userId; // Riferimento logico all'utente

    @Column(nullable = false, unique = true)
    private String orderNumber; // Es. ORD-20260601-XYZ

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(unique = true)
    private String stripeCheckoutSessionId;

    private String stripePaymentIntentId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}

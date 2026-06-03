package com.michelecampanello.springshop.domains.orders.controller;

import com.michelecampanello.springshop.domains.orders.dto.OrderResponse;
import com.michelecampanello.springshop.domains.orders.dto.UpdateOrderStatusRequest;
import com.michelecampanello.springshop.domains.orders.service.OrderService;
import com.michelecampanello.springshop.domains.users.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Trasforma il carrello corrente dell'utente in un ordine (Checkout).
     * POST /api/v1/orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal User user) {
        OrderResponse order = orderService.checkout(user.getId());
        return ResponseEntity.ok(order);
    }

    /**
     * Recupera lo storico degli ordini effettuati dall'utente loggato.
     * GET /api/v1/orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal User user) {
        List<OrderResponse> orders = orderService.getOrdersByUserId(user.getId());
        return ResponseEntity.ok(orders);
    }

    /**
     * Recupera un singolo ordine per ID (solo il proprietario o ADMIN).
     * GET /api/v1/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id, user));
    }

    /**
     * Aggiorna lo stato di un ordine (solo ADMIN).
     * PUT /api/v1/orders/{id}/status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.status()));
    }
}
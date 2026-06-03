package com.michelecampanello.springshop.domains.orders.repository;

import com.michelecampanello.springshop.domains.orders.model.Order;
import com.michelecampanello.springshop.domains.orders.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
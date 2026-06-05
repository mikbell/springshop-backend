package com.michelecampanello.springshop.domains.orders.repository;

import com.michelecampanello.springshop.domains.orders.model.Order;
import com.michelecampanello.springshop.domains.orders.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    List<Order> findTop5ByOrderByCreatedAtDesc();
    long countByStatus(OrderStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.status in :statuses")
    BigDecimal sumTotalAmountByStatusIn(@Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
            select coalesce(sum(o.totalAmount), 0)
            from Order o
            where o.status in :statuses and o.createdAt >= :createdAt
            """)
    BigDecimal sumTotalAmountByStatusInAndCreatedAtGreaterThanEqual(
            @Param("statuses") Collection<OrderStatus> statuses,
            @Param("createdAt") LocalDateTime createdAt);
}

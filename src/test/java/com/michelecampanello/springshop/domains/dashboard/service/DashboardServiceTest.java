package com.michelecampanello.springshop.domains.dashboard.service;

import com.michelecampanello.springshop.core.dto.PageResponse;
import com.michelecampanello.springshop.domains.dashboard.dto.DashboardSummaryResponse;
import com.michelecampanello.springshop.domains.dashboard.dto.LowStockProductResponse;
import com.michelecampanello.springshop.domains.dashboard.dto.RecentOrderResponse;
import com.michelecampanello.springshop.domains.orders.model.Order;
import com.michelecampanello.springshop.domains.orders.model.OrderStatus;
import com.michelecampanello.springshop.domains.orders.repository.OrderRepository;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import com.michelecampanello.springshop.domains.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock UserRepository userRepository;
    @Mock ProductRepository productRepository;
    @Mock OrderRepository orderRepository;

    @InjectMocks DashboardService dashboardService;

    @Test
    void getSummary_returnsAggregatedCountersAndRevenue() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByActiveTrue()).thenReturn(8L);
        when(productRepository.count()).thenReturn(20L);
        when(productRepository.countByStatus(Product.ProductStatus.AVAILABLE)).thenReturn(15L);
        when(productRepository.countByStatus(Product.ProductStatus.OUT_OF_STOCK)).thenReturn(2L);
        when(productRepository.countByStockQuantityLessThanEqual(5)).thenReturn(3L);
        when(orderRepository.count()).thenReturn(12L);
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(4L);
        when(orderRepository.countByStatus(OrderStatus.PAID)).thenReturn(5L);
        when(orderRepository.countByStatus(OrderStatus.SHIPPED)).thenReturn(2L);
        when(orderRepository.countByStatus(OrderStatus.CANCELLED)).thenReturn(1L);
        when(orderRepository.sumTotalAmountByStatusIn(List.of(OrderStatus.PAID, OrderStatus.SHIPPED)))
                .thenReturn(new BigDecimal("250.00"));
        when(orderRepository.sumTotalAmountByStatusInAndCreatedAtGreaterThanEqual(
                org.mockito.ArgumentMatchers.eq(List.of(OrderStatus.PAID, OrderStatus.SHIPPED)),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("75.00"));

        DashboardSummaryResponse result = dashboardService.getSummary();

        assertThat(result.totalUsers()).isEqualTo(10);
        assertThat(result.availableProducts()).isEqualTo(15);
        assertThat(result.paidOrders()).isEqualTo(5);
        assertThat(result.totalRevenue()).isEqualByComparingTo("250.00");
        assertThat(result.todayRevenue()).isEqualByComparingTo("75.00");
    }

    @Test
    void getRecentOrders_mapsOrdersToDashboardResponses() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setOrderNumber("ORD-TEST-001");
        order.setUserId(userId);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(new BigDecimal("40.00"));
        order.setCreatedAt(LocalDateTime.of(2026, 6, 5, 10, 30));

        when(orderRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of(order));

        List<RecentOrderResponse> result = dashboardService.getRecentOrders();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(orderId);
        assertThat(result.getFirst().orderNumber()).isEqualTo("ORD-TEST-001");
        assertThat(result.getFirst().totalAmount()).isEqualByComparingTo("40.00");
    }

    @Test
    void getLowStockProducts_normalizesNegativeThresholdAndReturnsPage() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setSku("SKU-001");
        product.setStockQuantity(0);
        product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
        PageRequest pageable = PageRequest.of(0, 10);

        when(productRepository.findByStockQuantityLessThanEqualOrderByStockQuantityAsc(0, pageable))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        PageResponse<LowStockProductResponse> result = dashboardService.getLowStockProducts(-3, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().sku()).isEqualTo("SKU-001");
        assertThat(result.getContent().getFirst().stockQuantity()).isZero();
    }
}

package com.michelecampanello.springshop.domains.dashboard.service;

import com.michelecampanello.springshop.core.dto.PageResponse;
import com.michelecampanello.springshop.domains.dashboard.dto.DashboardSummaryResponse;
import com.michelecampanello.springshop.domains.dashboard.dto.LowStockProductResponse;
import com.michelecampanello.springshop.domains.dashboard.dto.RecentOrderResponse;
import com.michelecampanello.springshop.domains.orders.model.OrderStatus;
import com.michelecampanello.springshop.domains.orders.repository.OrderRepository;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import com.michelecampanello.springshop.domains.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final List<OrderStatus> REVENUE_STATUSES = List.of(OrderStatus.PAID, OrderStatus.SHIPPED);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public DashboardSummaryResponse getSummary() {
        return new DashboardSummaryResponse(
                userRepository.count(),
                userRepository.countByActiveTrue(),
                productRepository.count(),
                productRepository.countByStatus(Product.ProductStatus.AVAILABLE),
                productRepository.countByStatus(Product.ProductStatus.OUT_OF_STOCK),
                productRepository.countByStockQuantityLessThanEqual(LOW_STOCK_THRESHOLD),
                orderRepository.count(),
                orderRepository.countByStatus(OrderStatus.PENDING),
                orderRepository.countByStatus(OrderStatus.PAID),
                orderRepository.countByStatus(OrderStatus.SHIPPED),
                orderRepository.countByStatus(OrderStatus.CANCELLED),
                orderRepository.sumTotalAmountByStatusIn(REVENUE_STATUSES),
                orderRepository.sumTotalAmountByStatusInAndCreatedAtGreaterThanEqual(
                        REVENUE_STATUSES,
                        LocalDate.now().atStartOfDay())
        );
    }

    public List<RecentOrderResponse> getRecentOrders() {
        return orderRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(order -> new RecentOrderResponse(
                        order.getId(),
                        order.getOrderNumber(),
                        order.getUserId(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getCreatedAt()))
                .toList();
    }

    public PageResponse<LowStockProductResponse> getLowStockProducts(int threshold, Pageable pageable) {
        int normalizedThreshold = Math.max(0, threshold);
        return PageResponse.from(productRepository
                .findByStockQuantityLessThanEqualOrderByStockQuantityAsc(normalizedThreshold, pageable)
                .map(product -> new LowStockProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getSku(),
                        product.getStockQuantity(),
                        product.getStatus())));
    }
}

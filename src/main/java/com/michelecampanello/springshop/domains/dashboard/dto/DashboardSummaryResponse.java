package com.michelecampanello.springshop.domains.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        long totalUsers,
        long activeUsers,
        long totalProducts,
        long availableProducts,
        long outOfStockProducts,
        long lowStockProducts,
        long totalOrders,
        long pendingOrders,
        long paidOrders,
        long shippedOrders,
        long cancelledOrders,
        BigDecimal totalRevenue,
        BigDecimal todayRevenue
) {
}

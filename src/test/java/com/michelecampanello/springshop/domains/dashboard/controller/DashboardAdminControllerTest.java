package com.michelecampanello.springshop.domains.dashboard.controller;

import com.michelecampanello.springshop.core.dto.PageResponse;
import com.michelecampanello.springshop.core.security.JwtService;
import com.michelecampanello.springshop.domains.dashboard.dto.DashboardSummaryResponse;
import com.michelecampanello.springshop.domains.dashboard.dto.LowStockProductResponse;
import com.michelecampanello.springshop.domains.dashboard.dto.RecentOrderResponse;
import com.michelecampanello.springshop.domains.dashboard.service.DashboardService;
import com.michelecampanello.springshop.domains.orders.model.OrderStatus;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.support.TestCacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestCacheConfig.class)
class DashboardAdminControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean DashboardService dashboardService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;

    @Test
    void getSummary_returnsDashboardSummary() throws Exception {
        when(dashboardService.getSummary()).thenReturn(new DashboardSummaryResponse(
                10, 8, 20, 15, 2, 3,
                12, 4, 5, 2, 1,
                new BigDecimal("250.00"), new BigDecimal("75.00")));

        mockMvc.perform(get("/api/v1/admin/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.activeUsers").value(8))
                .andExpect(jsonPath("$.totalRevenue").value(250.00))
                .andExpect(jsonPath("$.todayRevenue").value(75.00));
    }

    @Test
    void getRecentOrders_returnsLatestOrders() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(dashboardService.getRecentOrders()).thenReturn(List.of(new RecentOrderResponse(
                orderId,
                "ORD-TEST-001",
                UUID.randomUUID(),
                OrderStatus.PAID,
                new BigDecimal("40.00"),
                LocalDateTime.of(2026, 6, 5, 10, 30))));

        mockMvc.perform(get("/api/v1/admin/dashboard/recent-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId.toString()))
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-TEST-001"))
                .andExpect(jsonPath("$[0].status").value("PAID"));
    }

    @Test
    void getLowStockProducts_returnsPaginatedProducts() throws Exception {
        LowStockProductResponse product = new LowStockProductResponse(
                UUID.randomUUID(),
                "Test Product",
                "SKU-001",
                2,
                Product.ProductStatus.AVAILABLE);
        var page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);

        when(dashboardService.getLowStockProducts(eq(3), any())).thenReturn(PageResponse.from(page));

        mockMvc.perform(get("/api/v1/admin/dashboard/low-stock-products").param("threshold", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$.content[0].stockQuantity").value(2));
    }
}

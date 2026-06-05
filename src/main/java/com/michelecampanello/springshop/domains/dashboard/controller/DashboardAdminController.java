package com.michelecampanello.springshop.domains.dashboard.controller;

import com.michelecampanello.springshop.core.dto.PageResponse;
import com.michelecampanello.springshop.domains.dashboard.dto.DashboardSummaryResponse;
import com.michelecampanello.springshop.domains.dashboard.dto.LowStockProductResponse;
import com.michelecampanello.springshop.domains.dashboard.dto.RecentOrderResponse;
import com.michelecampanello.springshop.domains.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DashboardAdminController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<List<RecentOrderResponse>> getRecentOrders() {
        return ResponseEntity.ok(dashboardService.getRecentOrders());
    }

    @GetMapping("/low-stock-products")
    public ResponseEntity<PageResponse<LowStockProductResponse>> getLowStockProducts(
            @RequestParam(defaultValue = "5") int threshold,
            Pageable pageable) {
        return ResponseEntity.ok(dashboardService.getLowStockProducts(threshold, pageable));
    }
}

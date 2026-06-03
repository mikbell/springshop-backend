package com.michelecampanello.springshop.domains.orders.controller;

import com.michelecampanello.springshop.core.dto.PageResponse;
import com.michelecampanello.springshop.core.security.JwtService;
import com.michelecampanello.springshop.domains.orders.dto.OrderResponse;
import com.michelecampanello.springshop.support.TestCacheConfig;
import com.michelecampanello.springshop.domains.orders.model.OrderStatus;
import com.michelecampanello.springshop.domains.orders.service.OrderService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestCacheConfig.class)
class OrderAdminControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean OrderService orderService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;

    private OrderResponse buildResponse(OrderStatus status) {
        return new OrderResponse(UUID.randomUUID(), "ORD-TEST-001", UUID.randomUUID(),
                status, new BigDecimal("30.00"), List.of(), LocalDateTime.now());
    }

    @Test
    void getAllOrders_noFilter_returnsPaginatedList() throws Exception {
        OrderResponse r1 = buildResponse(OrderStatus.PENDING);
        OrderResponse r2 = buildResponse(OrderStatus.PAID);
        var page = new PageImpl<>(List.of(r1, r2), PageRequest.of(0, 10), 2);

        when(orderService.getAllOrders(eq(null), any())).thenReturn(PageResponse.from(page));

        mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getAllOrders_withStatusFilter_returnsFilteredList() throws Exception {
        OrderResponse r = buildResponse(OrderStatus.PENDING);
        var page = new PageImpl<>(List.of(r), PageRequest.of(0, 10), 1);

        when(orderService.getAllOrders(eq(OrderStatus.PENDING), any())).thenReturn(PageResponse.from(page));

        mockMvc.perform(get("/api/v1/admin/orders").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getAllOrders_emptyResult_returnsEmptyPage() throws Exception {
        var page = new PageImpl<OrderResponse>(List.of(), PageRequest.of(0, 10), 0);

        when(orderService.getAllOrders(eq(null), any())).thenReturn(PageResponse.from(page));

        mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}

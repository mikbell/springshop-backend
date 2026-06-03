package com.michelecampanello.springshop.domains.orders.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michelecampanello.springshop.core.exceptions.InvalidOrderStatusTransitionException;
import com.michelecampanello.springshop.core.exceptions.ResourceNotFoundException;
import com.michelecampanello.springshop.core.security.JwtService;
import com.michelecampanello.springshop.domains.orders.dto.OrderResponse;
import com.michelecampanello.springshop.domains.orders.dto.UpdateOrderStatusRequest;
import com.michelecampanello.springshop.domains.orders.model.OrderStatus;
import com.michelecampanello.springshop.domains.orders.service.OrderService;
import com.michelecampanello.springshop.domains.users.model.User;
import com.michelecampanello.springshop.support.TestCacheConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestCacheConfig.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean OrderService orderService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private User buildUser(UUID id, User.Role role) {
        User u = new User();
        u.setId(id);
        u.setEmail("test@test.it");
        u.setRole(role);
        return u;
    }

    private void authenticateAs(User user) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities())
        );
        SecurityContextHolder.setContext(ctx);
    }

    private OrderResponse buildOrderResponse(UUID id, UUID userId, OrderStatus status) {
        return new OrderResponse(id, "ORD-TEST-001", userId,
                status, new BigDecimal("50.00"), List.of(), LocalDateTime.now());
    }

    // ── GET /api/v1/orders/{id} ───────────────────────────────────────────────

    @Test
    void getOrder_owner_returns200() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, User.Role.CUSTOMER);
        OrderResponse response = buildOrderResponse(orderId, userId, OrderStatus.PENDING);

        when(orderService.getOrderById(eq(orderId), any(User.class))).thenReturn(response);
        authenticateAs(user);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-TEST-001"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getOrder_differentUser_returns403() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, User.Role.CUSTOMER);

        when(orderService.getOrderById(eq(orderId), any(User.class)))
                .thenThrow(new AuthorizationDeniedException("Non autorizzato", () -> false));
        authenticateAs(user);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrder_notFound_returns404() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, User.Role.CUSTOMER);

        when(orderService.getOrderById(eq(orderId), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Ordine non trovato: " + orderId));
        authenticateAs(user);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/v1/orders/{id}/status ────────────────────────────────────────

    @Test
    void updateStatus_validTransition_returns200() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User admin = buildUser(userId, User.Role.ADMIN);
        OrderResponse response = buildOrderResponse(orderId, userId, OrderStatus.PAID);

        when(orderService.updateOrderStatus(eq(orderId), eq(OrderStatus.PAID))).thenReturn(response);
        authenticateAs(admin);

        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(OrderStatus.PAID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void updateStatus_invalidTransition_returns400() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User admin = buildUser(userId, User.Role.ADMIN);

        when(orderService.updateOrderStatus(eq(orderId), eq(OrderStatus.PENDING)))
                .thenThrow(new InvalidOrderStatusTransitionException(OrderStatus.SHIPPED, OrderStatus.PENDING));
        authenticateAs(admin);

        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(OrderStatus.PENDING))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_missingBody_returns400() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User admin = buildUser(userId, User.Role.ADMIN);
        authenticateAs(admin);

        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

package com.michelecampanello.springshop.domains.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michelecampanello.springshop.core.security.JwtService;
import com.michelecampanello.springshop.domains.products.dto.ProductRequest;
import com.michelecampanello.springshop.domains.products.dto.ProductResponse;
import com.michelecampanello.springshop.domains.products.model.Product.ProductStatus;
import com.michelecampanello.springshop.domains.products.service.ProductService;
import com.michelecampanello.springshop.domains.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.michelecampanello.springshop.support.TestCacheConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ProductControllerAuthTest.MethodSecurityConfig.class, TestCacheConfig.class})
class ProductControllerAuthTest {

    // @WebMvcTest with addFilters=false bypasses SecurityConfig, so method security is NOT
    // automatically active. This inner config re-enables it so @PreAuthorize is enforced.
    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    @Autowired MockMvc mockMvc;
    @MockitoBean ProductService productService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ProductRequest validRequest() {
        return new ProductRequest("Prodotto Test", "desc", new BigDecimal("9.99"), 10, "SKU-001", null);
    }

    private ProductResponse stubResponse(UUID id) {
        return new ProductResponse(id, "Prodotto Test", "desc",
                new BigDecimal("9.99"), 10, "SKU-001", "prodotto-test",
                null, ProductStatus.AVAILABLE, null, null);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerNonPuoCreareProdotto() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPuoCreareProdotto() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.createProduct(any())).thenReturn(stubResponse(id));

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerNonPuoAggiornaProdotto() throws Exception {
        mockMvc.perform(put("/api/v1/products/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerNonPuoEliminaProdotto() throws Exception {
        mockMvc.perform(delete("/api/v1/products/" + UUID.randomUUID()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPuoAggiornaProdotto() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.updateProduct(eq(id), any())).thenReturn(stubResponse(id));

        mockMvc.perform(put("/api/v1/products/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPuoEliminaProdotto() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/products/" + id))
            .andExpect(status().isNoContent());
    }
}

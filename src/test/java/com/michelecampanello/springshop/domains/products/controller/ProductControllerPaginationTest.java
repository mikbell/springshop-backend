package com.michelecampanello.springshop.domains.products.controller;

import com.michelecampanello.springshop.core.dto.PageResponse;
import com.michelecampanello.springshop.core.security.JwtService;
import com.michelecampanello.springshop.domains.products.dto.ProductResponse;
import com.michelecampanello.springshop.domains.products.model.Product.ProductStatus;
import com.michelecampanello.springshop.domains.products.service.ProductService;
import com.michelecampanello.springshop.domains.users.service.UserService;
import com.michelecampanello.springshop.support.TestCacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestCacheConfig.class)
class ProductControllerPaginationTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ProductService productService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserService userService;

    private ProductResponse buildResponse() {
        return new ProductResponse(UUID.randomUUID(), "Prodotto Test", "desc",
                new BigDecimal("9.99"), 10, "SKU-001", "prodotto-test",
                null, ProductStatus.AVAILABLE, null, null);
    }

    @Test
    void getProducts_noParams_returns200WithPageStructure() throws Exception {
        ProductResponse r = buildResponse();
        var page = new PageImpl<>(List.of(r), PageRequest.of(0, 20), 1);

        when(productService.getProducts(any(), any())).thenReturn(PageResponse.from(page));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("SKU-001"));
    }

    @Test
    void getProducts_withSearchTerm_returns200() throws Exception {
        var page = new PageImpl<ProductResponse>(List.of(), PageRequest.of(0, 20), 0);

        when(productService.getProducts(any(), any())).thenReturn(PageResponse.from(page));

        mockMvc.perform(get("/api/v1/products").param("searchTerm", "borsa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getProducts_withPageAndSize_returns200() throws Exception {
        var page = new PageImpl<ProductResponse>(List.of(), PageRequest.of(1, 5), 0);

        when(productService.getProducts(any(), any())).thenReturn(PageResponse.from(page));

        mockMvc.perform(get("/api/v1/products")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void searchEndpoint_isGone_noGetHandler() throws Exception {
        // GET /api/v1/products/search matches /{id} path-variable routes (PUT, DELETE)
        // but has no GET handler — Spring returns 405 (Method Not Allowed), confirming the /search GET endpoint is removed
        mockMvc.perform(get("/api/v1/products/search"))
                .andExpect(status().isMethodNotAllowed());
    }
}

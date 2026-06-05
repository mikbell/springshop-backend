// src/test/java/com/michelecampanello/springshop/domains/products/service/ProductServiceTest.java
package com.michelecampanello.springshop.domains.products.service;

import com.michelecampanello.springshop.core.dto.PageResponse;
import com.michelecampanello.springshop.domains.categories.repository.CategoryRepository;
import com.michelecampanello.springshop.domains.products.dto.ProductRequest;
import com.michelecampanello.springshop.domains.products.dto.ProductResponse;
import com.michelecampanello.springshop.domains.products.dto.ProductSearchCriteria;
import com.michelecampanello.springshop.domains.products.mapper.ProductMapper;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.products.model.Product.ProductStatus;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import com.michelecampanello.springshop.domains.reviews.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock ProductMapper productMapper;
    @Mock ProductImageStorageService productImageStorageService;
    @Mock ReviewRepository reviewRepository;

    @InjectMocks ProductService productService;

    private ProductResponse buildResponse(UUID id) {
        return new ProductResponse(id, "Test", "desc", new BigDecimal("9.99"), 10,
                "SKU-001", "test", null, ProductStatus.AVAILABLE, null, null);
    }

    @Test
    void getProducts_noFilters_returnsPagedResults() {
        Product product = new Product();
        ProductResponse response = buildResponse(UUID.randomUUID());
        PageRequest pageable = PageRequest.of(0, 20);
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, null, null, null);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(productMapper.toResponse(product)).thenReturn(response);

        PageResponse<ProductResponse> result = productService.getProducts(criteria, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getProducts_withSearchTerm_callsRepositoryWithSpec() {
        PageRequest pageable = PageRequest.of(0, 20);
        ProductSearchCriteria criteria = new ProductSearchCriteria("borsa", null, null, null);
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        PageResponse<ProductResponse> result = productService.getProducts(criteria, pageable);

        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getProducts_secondPage_returnsPaginatedSlice() {
        PageRequest pageable = PageRequest.of(1, 5);
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, null, null, null);
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        PageResponse<ProductResponse> result = productService.getProducts(criteria, pageable);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);
    }

    @Test
    void createProduct_withImage_usesUploadedImageUrl() {
        ProductRequest request = new ProductRequest("Test", "desc", BigDecimal.TEN, 10, "SKU-IMG", "https://old.example/img.png");
        MockMultipartFile image = new MockMultipartFile("image", "product.png", "image/png", "image".getBytes());
        Product product = new Product();
        ProductResponse response = new ProductResponse(UUID.randomUUID(), "Test", "desc", BigDecimal.TEN, 10,
                "SKU-IMG", "test", "/uploads/products/generated.png", ProductStatus.AVAILABLE, null, null);

        when(productRepository.findBySku("SKU-IMG")).thenReturn(java.util.Optional.empty());
        when(productRepository.findBySlug("test")).thenReturn(java.util.Optional.empty());
        when(productMapper.toEntity(request)).thenReturn(product);
        when(productImageStorageService.store(image)).thenReturn("/uploads/products/generated.png");
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.createProduct(request, image);

        assertThat(product.getImageUrl()).isEqualTo("/uploads/products/generated.png");
        assertThat(result.imageUrl()).isEqualTo("/uploads/products/generated.png");
        verify(productImageStorageService).store(image);
    }

    @Test
    void deleteProduct_existingProduct_deletesReviewsBeforeProduct() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(true);

        productService.deleteProduct(productId);

        var ordered = org.mockito.Mockito.inOrder(reviewRepository, productRepository);
        ordered.verify(reviewRepository).deleteByProductId(productId);
        ordered.verify(productRepository).deleteById(productId);
    }
}

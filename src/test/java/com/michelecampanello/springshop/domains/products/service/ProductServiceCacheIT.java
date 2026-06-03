package com.michelecampanello.springshop.domains.products.service;

import com.michelecampanello.springshop.domains.products.dto.ProductRequest;
import com.michelecampanello.springshop.domains.products.dto.ProductResponse;
import com.michelecampanello.springshop.domains.products.dto.ProductSearchCriteria;
import com.michelecampanello.springshop.domains.products.mapper.ProductMapper;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.products.model.Product.ProductStatus;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("integration")
@Testcontainers
class ProductServiceCacheIT {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired ProductService productService;
    @MockitoBean ProductRepository productRepository;
    @MockitoBean ProductMapper productMapper;
    @Autowired CacheManager cacheManager;
    @Autowired StringRedisTemplate redisTemplate;

    private static final ProductSearchCriteria EMPTY_CRITERIA =
            new ProductSearchCriteria(null, null, null, null);
    private static final PageRequest DEFAULT_PAGE =
            PageRequest.of(0, 20, Sort.by("createdAt").descending());

    @BeforeEach
    void clearCaches() {
        // Warm up the Redis connection to ensure it is established before test assertions
        redisTemplate.opsForValue().get("__warmup__");
        cacheManager.getCacheNames()
                .forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    @Test
    void getProducts_secondCall_hitsCache() {
        when(productRepository.findAll(any(Specification.class), eq(DEFAULT_PAGE)))
                .thenReturn(Page.empty(DEFAULT_PAGE));

        productService.getProducts(EMPTY_CRITERIA, DEFAULT_PAGE);
        productService.getProducts(EMPTY_CRITERIA, DEFAULT_PAGE);

        verify(productRepository, times(1)).findAll(any(Specification.class), eq(DEFAULT_PAGE));
    }

    @Test
    void getProductById_secondCall_hitsCache() {
        UUID id = UUID.randomUUID();
        Product mockProduct = mock(Product.class);
        ProductResponse response = buildResponse(id, "SKU-A");
        when(productRepository.findById(id)).thenReturn(Optional.of(mockProduct));
        when(productMapper.toResponse(mockProduct)).thenReturn(response);

        productService.getProductById(id);
        productService.getProductById(id);

        verify(productRepository, times(1)).findById(id);
    }

    @Test
    void getProductBySku_secondCall_hitsCache() {
        UUID id = UUID.randomUUID();
        Product mockProduct = mock(Product.class);
        ProductResponse response = buildResponse(id, "SKU-B");
        when(productRepository.findBySku("SKU-B")).thenReturn(Optional.of(mockProduct));
        when(productMapper.toResponse(mockProduct)).thenReturn(response);

        productService.getProductBySku("SKU-B");
        productService.getProductBySku("SKU-B");

        verify(productRepository, times(1)).findBySku("SKU-B");
    }

    @Test
    void createProduct_evictsProductsListCache() {
        when(productRepository.findAll(any(Specification.class), eq(DEFAULT_PAGE)))
                .thenReturn(Page.empty(DEFAULT_PAGE));
        productService.getProducts(EMPTY_CRITERIA, DEFAULT_PAGE);

        ProductRequest req = new ProductRequest("Test", null, BigDecimal.TEN, 5, "SKU-NEW", null);
        Product mockProduct = mock(Product.class);
        ProductResponse response = buildResponse(UUID.randomUUID(), "SKU-NEW");
        when(productRepository.findBySku("SKU-NEW")).thenReturn(Optional.empty());
        when(productMapper.toEntity(req)).thenReturn(mockProduct);
        when(productRepository.save(mockProduct)).thenReturn(mockProduct);
        when(productMapper.toResponse(mockProduct)).thenReturn(response);
        productService.createProduct(req);

        productService.getProducts(EMPTY_CRITERIA, DEFAULT_PAGE);
        verify(productRepository, times(2)).findAll(any(Specification.class), eq(DEFAULT_PAGE));
    }

    @Test
    void updateProduct_evictsSingleProductCache() {
        UUID id = UUID.randomUUID();
        Product mockProduct = mock(Product.class);
        when(mockProduct.getSku()).thenReturn("SKU-C");
        ProductResponse response = buildResponse(id, "SKU-C");
        when(productRepository.findById(id)).thenReturn(Optional.of(mockProduct));
        when(productMapper.toResponse(mockProduct)).thenReturn(response);
        productService.getProductById(id);

        ProductRequest updateReq = new ProductRequest("Updated", null, BigDecimal.ONE, 3, "SKU-C", null);
        when(productRepository.save(mockProduct)).thenReturn(mockProduct);
        productService.updateProduct(id, updateReq);

        productService.getProductById(id);
        // 1st: initial getProductById (cache miss), 2nd: updateProduct → getProductOrThrow,
        // 3rd: post-eviction getProductById (cache miss after eviction)
        verify(productRepository, times(3)).findById(id);
    }

    @Test
    void deleteProduct_evictsAllCaches() {
        UUID id = UUID.randomUUID();
        Product mockProduct = mock(Product.class);
        when(mockProduct.getSku()).thenReturn("SKU-D");
        ProductResponse response = buildResponse(id, "SKU-D");
        when(productRepository.findAll(any(Specification.class), eq(DEFAULT_PAGE)))
                .thenReturn(Page.empty(DEFAULT_PAGE));
        when(productRepository.findById(id)).thenReturn(Optional.of(mockProduct));
        when(productMapper.toResponse(mockProduct)).thenReturn(response);
        productService.getProducts(EMPTY_CRITERIA, DEFAULT_PAGE);
        productService.getProductById(id);

        when(productRepository.existsById(id)).thenReturn(true);
        productService.deleteProduct(id);

        productService.getProducts(EMPTY_CRITERIA, DEFAULT_PAGE);
        productService.getProductById(id);
        verify(productRepository, times(2)).findAll(any(Specification.class), eq(DEFAULT_PAGE));
        verify(productRepository, times(2)).findById(id);
    }

    private ProductResponse buildResponse(UUID id, String sku) {
        return new ProductResponse(id, "Test", null, BigDecimal.TEN, 5, sku, "test-slug",
                null, ProductStatus.AVAILABLE, null, null);
    }
}

package com.michelecampanello.springshop.domains.products.service;

import com.michelecampanello.springshop.core.dto.PageResponse;
import com.michelecampanello.springshop.core.exceptions.DuplicateResourceException;
import com.michelecampanello.springshop.core.exceptions.ImmutableFieldException;
import com.michelecampanello.springshop.core.exceptions.ResourceNotFoundException;
import com.michelecampanello.springshop.core.util.SlugGenerator;
import com.michelecampanello.springshop.domains.categories.repository.CategoryRepository;
import com.michelecampanello.springshop.domains.products.dto.ProductRequest;
import com.michelecampanello.springshop.domains.products.dto.ProductResponse;
import com.michelecampanello.springshop.domains.products.dto.ProductSearchCriteria;
import com.michelecampanello.springshop.domains.products.mapper.ProductMapper;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import com.michelecampanello.springshop.domains.products.repository.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductImageStorageService productImageStorageService;

    @Cacheable(cacheNames = "products", keyGenerator = "productListKeyGenerator")
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(ProductSearchCriteria criteria, Pageable pageable) {
        Specification<Product> spec = ProductSpecifications.filterByCriteria(criteria);
        Page<ProductResponse> page = productRepository.findAll(spec, pageable).map(productMapper::toResponse);
        return PageResponse.from(page);
    }

    @Cacheable(cacheNames = "product", key = "'id::' + #id")
    public ProductResponse getProductById(UUID id) {
        return productMapper.toResponse(getProductOrThrow(id));
    }

    @Cacheable(cacheNames = "product", key = "'sku::' + #sku")
    public ProductResponse getProductBySku(String sku) {
        return productMapper.toResponse(productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato con SKU: " + sku)));
    }

    @Cacheable(cacheNames = "product", key = "'slug::' + #slug")
    public ProductResponse getProductBySlug(String slug) {
        return productMapper.toResponse(productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato con slug: " + slug)));
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest req) {
        return createProduct(req, null);
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest req, MultipartFile image) {
        if (productRepository.findBySku(req.sku()).isPresent()) {
            throw new DuplicateResourceException("Un prodotto con questo codice SKU è già presente nel catalogo.");
        }
        Product product = productMapper.toEntity(req);
        String uploadedImageUrl = productImageStorageService.store(image);
        if (uploadedImageUrl != null) {
            product.setImageUrl(uploadedImageUrl);
        }
        product.setSlug(generateUniqueSlug(req.name()));
        product.setCategory(resolveCategory(req.categoryId()));
        return productMapper.toResponse(productRepository.save(product));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "products", allEntries = true),
            @CacheEvict(cacheNames = "product", key = "'id::' + #id"),
            @CacheEvict(cacheNames = "product", key = "'sku::' + #req.sku()")
    })
    public ProductResponse updateProduct(UUID id, ProductRequest req) {
        Product product = getProductOrThrow(id);
        if (!product.getSku().equalsIgnoreCase(req.sku())) {
            throw new ImmutableFieldException("Il codice SKU non può essere modificato dopo la creazione del prodotto.");
        }
        productMapper.updateEntity(product, req);
        product.setCategory(resolveCategory(req.categoryId()));
        return productMapper.toResponse(productRepository.save(product));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "products", allEntries = true),
            @CacheEvict(cacheNames = "product", allEntries = true)
    })
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prodotto non trovato: " + id);
        }
        productRepository.deleteById(id);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "products", allEntries = true),
            @CacheEvict(cacheNames = "product", key = "'id::' + #productId")
    })
    @Transactional
    public ProductResponse updateProductStock(UUID productId, Integer newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato"));
        product.setStockQuantity(newStock);
        return productMapper.toResponse(productRepository.save(product));
    }

    private Product getProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato: " + id));
    }

    private com.michelecampanello.springshop.domains.categories.model.Category resolveCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria non trovata: " + categoryId));
    }

    private String generateUniqueSlug(String name) {
        String base = SlugGenerator.toSlug(name);
        if (base.isEmpty()) base = "product";
        String candidate = base;
        int suffix = 2;
        while (productRepository.findBySlug(candidate).isPresent()) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }
}

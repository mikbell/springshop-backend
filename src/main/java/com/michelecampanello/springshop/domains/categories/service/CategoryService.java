package com.michelecampanello.springshop.domains.categories.service;

import com.michelecampanello.springshop.core.exceptions.DuplicateResourceException;
import com.michelecampanello.springshop.core.exceptions.ImmutableFieldException;
import com.michelecampanello.springshop.core.exceptions.ResourceNotFoundException;
import com.michelecampanello.springshop.core.util.SlugGenerator;
import com.michelecampanello.springshop.domains.categories.dto.CategoryRequest;
import com.michelecampanello.springshop.domains.categories.dto.CategoryResponse;
import com.michelecampanello.springshop.domains.categories.mapper.CategoryMapper;
import com.michelecampanello.springshop.domains.categories.model.Category;
import com.michelecampanello.springshop.domains.categories.repository.CategoryRepository;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        return categoryMapper.toResponse(categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria non trovata con slug: " + slug)));
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        return categoryMapper.toResponse(getCategoryOrThrow(id));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Una categoria con questo nome esiste gia.");
        }
        Category category = categoryMapper.toEntity(request);
        category.setSlug(generateUniqueSlug(request.name()));
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "products", allEntries = true),
            @CacheEvict(cacheNames = "product", allEntries = true)
    })
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        Category category = getCategoryOrThrow(id);
        if (!category.getName().equalsIgnoreCase(request.name())) {
            throw new ImmutableFieldException("Il nome della categoria non puo essere modificato dopo la creazione.");
        }
        categoryMapper.updateEntity(category, request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "products", allEntries = true),
            @CacheEvict(cacheNames = "product", allEntries = true)
    })
    @Transactional
    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria non trovata: " + id);
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new ImmutableFieldException("La categoria non puo essere eliminata perche contiene prodotti.");
        }
        categoryRepository.deleteById(id);
    }

    private Category getCategoryOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria non trovata: " + id));
    }

    private String generateUniqueSlug(String name) {
        String base = SlugGenerator.toSlug(name);
        if (base.isEmpty()) {
            base = "category";
        }
        String candidate = base;
        int suffix = 2;
        while (categoryRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }
}

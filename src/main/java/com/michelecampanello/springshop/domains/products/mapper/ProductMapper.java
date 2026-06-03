package com.michelecampanello.springshop.domains.products.mapper;

import com.michelecampanello.springshop.domains.categories.dto.CategorySummaryResponse;
import com.michelecampanello.springshop.domains.products.dto.ProductRequest;
import com.michelecampanello.springshop.domains.products.dto.ProductResponse;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.reviews.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    private final ReviewRepository reviewRepository;

    public ProductMapper() {
        this.reviewRepository = null;
    }

    @Autowired
    public ProductMapper(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Product toEntity(ProductRequest req) {
        Product product = new Product();
        product.setSku(req.sku());
        updateEntity(product, req);
        return product;
    }

    /** Non tocca lo SKU (immutabile). */
    public void updateEntity(Product product, ProductRequest req) {
        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setImageUrl(req.imageUrl());
        product.setStockQuantity(req.stockQuantity());
    }

    public ProductResponse toResponse(Product product) {
        Double averageRating = null;
        long reviewCount = 0L;
        if (reviewRepository != null && product.getId() != null) {
            averageRating = reviewRepository.getAverageRatingByProductId(product.getId());
            reviewCount = reviewRepository.countByProductId(product.getId());
        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getSku(),
                product.getSlug(),
                product.getImageUrl(),
                product.getCategory() == null ? null : new CategorySummaryResponse(
                        product.getCategory().getId(),
                        product.getCategory().getName(),
                        product.getCategory().getSlug()
                ),
                product.getStatus(),
                averageRating,
                reviewCount,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}

package com.michelecampanello.springshop.domains.products.mapper;

import com.michelecampanello.springshop.domains.products.dto.ProductRequest;
import com.michelecampanello.springshop.domains.products.dto.ProductResponse;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.reviews.repository.ReviewRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductMapperTest {

    private final ProductMapper mapper = new ProductMapper();

    @Test
    void toEntityCopiesFieldsAndSku() {
        ProductRequest req = new ProductRequest("Maglietta", "Desc",
                new BigDecimal("9.99"), 5, "SKU-1", "http://img");

        Product product = mapper.toEntity(req);

        assertThat(product.getName()).isEqualTo("Maglietta");
        assertThat(product.getSku()).isEqualTo("SKU-1");
        assertThat(product.getStockQuantity()).isEqualTo(5);
        assertThat(product.getPrice()).isEqualByComparingTo("9.99");
    }

    @Test
    void updateEntityDoesNotTouchSku() {
        Product product = new Product();
        product.setSku("SKU-ORIG");
        ProductRequest req = new ProductRequest("Nuovo", "Desc",
                new BigDecimal("1.00"), 1, "SKU-DIVERSO", null);

        mapper.updateEntity(product, req);

        assertThat(product.getSku()).isEqualTo("SKU-ORIG");
        assertThat(product.getName()).isEqualTo("Nuovo");
    }

    @Test
    void toResponseCopiesSlug() {
        Product product = new Product();
        product.setName("Maglietta");
        product.setSku("SKU-1");
        product.setSlug("maglietta");
        product.setPrice(new BigDecimal("9.99"));
        product.setStockQuantity(5);

        ProductResponse response = mapper.toResponse(product);

        assertThat(response.name()).isEqualTo("Maglietta");
        assertThat(response.slug()).isEqualTo("maglietta");
        assertThat(response.sku()).isEqualTo("SKU-1");
    }

    @Test
    void toResponseIncludesReviewStats() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setName("Maglietta");
        product.setSku("SKU-1");
        product.setSlug("maglietta");
        product.setPrice(new BigDecimal("9.99"));
        product.setStockQuantity(5);

        ReviewRepository reviewRepository = mock(ReviewRepository.class);
        when(reviewRepository.getAverageRatingByProductId(productId)).thenReturn(4.5);
        when(reviewRepository.countByProductId(productId)).thenReturn(2L);
        ProductMapper mapper = new ProductMapper(reviewRepository);

        ProductResponse response = mapper.toResponse(product);

        assertThat(response.averageRating()).isEqualTo(4.5);
        assertThat(response.reviewCount()).isEqualTo(2L);
    }
}

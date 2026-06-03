package com.michelecampanello.springshop.domains.products.mapper;

import com.michelecampanello.springshop.domains.products.dto.ProductRequest;
import com.michelecampanello.springshop.domains.products.dto.ProductResponse;
import com.michelecampanello.springshop.domains.products.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

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
}

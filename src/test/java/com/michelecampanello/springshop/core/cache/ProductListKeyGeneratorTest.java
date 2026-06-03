package com.michelecampanello.springshop.core.cache;

import com.michelecampanello.springshop.domains.products.dto.ProductSearchCriteria;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class ProductListKeyGeneratorTest {

    private final ProductListKeyGenerator generator = new ProductListKeyGenerator();
    private final Method dummyMethod;

    ProductListKeyGeneratorTest() throws NoSuchMethodException {
        dummyMethod = Object.class.getMethod("toString");
    }

    @Test
    void sameParams_produceSameKey() {
        var criteria = new ProductSearchCriteria(null, null, null, null);
        var page = PageRequest.of(0, 20, Sort.by("createdAt").descending());

        Object key1 = generator.generate(this, dummyMethod, criteria, page);
        Object key2 = generator.generate(this, dummyMethod, criteria, page);

        assertThat(key1).isEqualTo(key2);
    }

    @Test
    void differentPageNumber_producesDifferentKey() {
        var criteria = new ProductSearchCriteria(null, null, null, null);
        var page0 = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        var page1 = PageRequest.of(1, 20, Sort.by("createdAt").descending());

        Object key0 = generator.generate(this, dummyMethod, criteria, page0);
        Object key1 = generator.generate(this, dummyMethod, criteria, page1);

        assertThat(key0).isNotEqualTo(key1);
    }

    @Test
    void differentSearchTerm_producesDifferentKey() {
        var empty = new ProductSearchCriteria(null, null, null, null);
        var withTerm = new ProductSearchCriteria("phone", null, null, null);
        var page = PageRequest.of(0, 20, Sort.by("createdAt").descending());

        Object key1 = generator.generate(this, dummyMethod, empty, page);
        Object key2 = generator.generate(this, dummyMethod, withTerm, page);

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void key_containsDoubleColonSeparator() {
        var criteria = new ProductSearchCriteria(null, null, null, null);
        var page = PageRequest.of(0, 20, Sort.by("createdAt").descending());

        String key = generator.generate(this, dummyMethod, criteria, page).toString();

        assertThat(key).contains("::");
    }

    @Test
    void key_containsVersionPrefix() {
        var criteria = new ProductSearchCriteria(null, null, null, null);
        var page = PageRequest.of(0, 20, Sort.by("createdAt").descending());

        String key = generator.generate(this, dummyMethod, criteria, page).toString();

        assertThat(key).startsWith("v2::");
    }
}

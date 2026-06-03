package com.michelecampanello.springshop.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;

/**
 * Shared test configuration that provides a no-op CacheManager for @WebMvcTest slices.
 * Import this class in @WebMvcTest tests that would otherwise fail because
 * @EnableCaching on SpringShopApplication requires a CacheManager bean.
 */
@TestConfiguration
public class TestCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }
}

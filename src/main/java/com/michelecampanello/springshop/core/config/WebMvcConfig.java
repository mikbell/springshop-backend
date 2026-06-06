package com.michelecampanello.springshop.core.config;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String productImagesDir;
    private final String publicPath;

    public WebMvcConfig(
            @Value("${app.upload.product-images-dir:uploads/products}") String productImagesDir,
            @Value("${app.upload.public-path:/uploads/products}") String publicPath) {
        this.productImagesDir = productImagesDir;
        this.publicPath = publicPath;
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String pattern = publicPath.endsWith("/") ? publicPath + "**" : publicPath + "/**";
        String location = Path.of(productImagesDir).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler(pattern).addResourceLocations(location);
    }
}

package com.michelecampanello.springshop.domains.products.service;

import com.michelecampanello.springshop.core.exceptions.InvalidFileUploadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductImageStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void store_savesImageAndReturnsPublicUrl() throws Exception {
        ProductImageStorageService storageService =
                new ProductImageStorageService(tempDir.toString(), "/uploads/products");
        MockMultipartFile image = new MockMultipartFile("image", "product.png", "image/png", "image".getBytes());

        String imageUrl = storageService.store(image);

        assertThat(imageUrl).startsWith("/uploads/products/").endsWith(".png");
        String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
    }

    @Test
    void store_rejectsNonImageFile() {
        ProductImageStorageService storageService =
                new ProductImageStorageService(tempDir.toString(), "/uploads/products");
        MockMultipartFile file = new MockMultipartFile("image", "product.txt", "text/plain", "text".getBytes());

        assertThatThrownBy(() -> storageService.store(file))
                .isInstanceOf(InvalidFileUploadException.class)
                .hasMessageContaining("immagine");
    }
}

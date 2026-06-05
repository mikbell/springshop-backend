package com.michelecampanello.springshop.domains.products.service;

import com.michelecampanello.springshop.core.exceptions.InvalidFileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductImageStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final Path uploadDir;
    private final String publicPath;

    public ProductImageStorageService(
            @Value("${app.upload.product-images-dir:uploads/products}") String uploadDir,
            @Value("${app.upload.public-path:/uploads/products}") String publicPath) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        this.publicPath = publicPath.endsWith("/") ? publicPath.substring(0, publicPath.length() - 1) : publicPath;
    }

    public String store(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        validateImage(image);

        String extension = getExtension(image.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;
        Path destination = uploadDir.resolve(filename).normalize();
        if (!destination.startsWith(uploadDir)) {
            throw new InvalidFileUploadException("Nome file immagine non valido.");
        }

        try {
            Files.createDirectories(uploadDir);
            Files.copy(image.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return publicPath + "/" + filename;
        } catch (IOException ex) {
            throw new InvalidFileUploadException("Impossibile salvare l'immagine del prodotto.", ex);
        }
    }

    private void validateImage(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new InvalidFileUploadException("Il file caricato deve essere un'immagine.");
        }

        String extension = getExtension(image.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileUploadException("Formato immagine non supportato. Usa jpg, png, webp o gif.");
        }
    }

    private String getExtension(String originalFilename) {
        String filename = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new InvalidFileUploadException("L'immagine deve avere un'estensione valida.");
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}


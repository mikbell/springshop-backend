package com.michelecampanello.springshop.core.config;

import com.michelecampanello.springshop.core.util.SlugGenerator;
import com.michelecampanello.springshop.domains.categories.model.Category;
import com.michelecampanello.springshop.domains.categories.repository.CategoryRepository;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.products.model.Product.ProductStatus;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final TransactionTemplate transactionTemplate;

    @Bean
    CommandLineRunner seedCatalogData() {
        return args -> transactionTemplate.executeWithoutResult(status -> seed());
    }

    private void seed() {
        List<CategorySeed> categories = List.of(
                new CategorySeed("Elettronica", "Dispositivi, accessori e tecnologia per l'uso quotidiano."),
                new CategorySeed("Abbigliamento", "Capi casual e accessori per ogni stagione."),
                new CategorySeed("Casa", "Prodotti utili per arredare e organizzare gli spazi domestici."),
                new CategorySeed("Sport", "Attrezzatura e accessori per allenamento e tempo libero.")
        );

        categories.forEach(this::createCategoryIfMissing);

        List<ProductSeed> products = List.of(
                new ProductSeed(
                        "Cuffie Wireless Pro",
                        "Cuffie Bluetooth con cancellazione del rumore e custodia di ricarica.",
                        "SKU-ELE-001",
                        "Elettronica",
                        "89.90",
                        35,
                        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e"
                ),
                new ProductSeed(
                        "Smartwatch Fit",
                        "Orologio smart con monitoraggio attivita, notifiche e autonomia estesa.",
                        "SKU-ELE-002",
                        "Elettronica",
                        "129.00",
                        18,
                        "https://images.unsplash.com/photo-1523275335684-37898b6baf30"
                ),
                new ProductSeed(
                        "T-Shirt Cotone Basic",
                        "T-shirt girocollo in cotone morbido, vestibilita regular.",
                        "SKU-ABB-001",
                        "Abbigliamento",
                        "19.90",
                        80,
                        "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab"
                ),
                new ProductSeed(
                        "Zaino Urban 24L",
                        "Zaino compatto con tasca laptop e scomparti organizzati.",
                        "SKU-ABB-002",
                        "Abbigliamento",
                        "49.90",
                        24,
                        "https://images.unsplash.com/photo-1553062407-98eeb64c6a62"
                ),
                new ProductSeed(
                        "Lampada da Tavolo LED",
                        "Lampada orientabile con luce regolabile per scrivania e comodino.",
                        "SKU-CAS-001",
                        "Casa",
                        "34.50",
                        42,
                        "https://images.unsplash.com/photo-1507473885765-e6ed057f782c"
                ),
                new ProductSeed(
                        "Set Contenitori Cucina",
                        "Set di contenitori ermetici impilabili per dispensa e frigorifero.",
                        "SKU-CAS-002",
                        "Casa",
                        "27.90",
                        55,
                        "https://images.unsplash.com/photo-1556911220-bff31c812dba"
                ),
                new ProductSeed(
                        "Tappetino Yoga Grip",
                        "Tappetino antiscivolo leggero, ideale per yoga, stretching e pilates.",
                        "SKU-SPO-001",
                        "Sport",
                        "29.90",
                        30,
                        "https://images.unsplash.com/photo-1592432678016-e910b452f9a2"
                ),
                new ProductSeed(
                        "Borraccia Termica 750ml",
                        "Borraccia in acciaio con isolamento termico per palestra e outdoor.",
                        "SKU-SPO-002",
                        "Sport",
                        "22.90",
                        65,
                        "https://images.unsplash.com/photo-1602143407151-7111542de6e8"
                )
        );

        products.forEach(this::createProductIfMissing);
    }

    private void createCategoryIfMissing(CategorySeed seed) {
        String slug = SlugGenerator.toSlug(seed.name());
        if (categoryRepository.existsBySlug(slug)) {
            return;
        }

        Category category = new Category();
        category.setName(seed.name());
        category.setSlug(slug);
        category.setDescription(seed.description());
        categoryRepository.save(category);
    }

    private void createProductIfMissing(ProductSeed seed) {
        if (productRepository.findBySku(seed.sku()).isPresent()) {
            return;
        }

        Category category = categoryRepository.findBySlug(SlugGenerator.toSlug(seed.categoryName()))
                .orElseThrow(() -> new IllegalStateException("Categoria seed non trovata: " + seed.categoryName()));

        Product product = Product.builder()
                .name(seed.name())
                .description(seed.description())
                .sku(seed.sku())
                .slug(generateProductSlug(seed.name()))
                .category(category)
                .price(new BigDecimal(seed.price()))
                .stockQuantity(seed.stockQuantity())
                .imageUrl(seed.imageUrl())
                .status(ProductStatus.AVAILABLE)
                .build();

        productRepository.save(product);
    }

    private String generateProductSlug(String name) {
        String base = SlugGenerator.toSlug(name);
        if (base.isEmpty()) {
            base = "product";
        }

        String candidate = base;
        int suffix = 2;
        while (productRepository.findBySlug(candidate).isPresent()) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private record CategorySeed(String name, String description) {
    }

    private record ProductSeed(
            String name,
            String description,
            String sku,
            String categoryName,
            String price,
            Integer stockQuantity,
            String imageUrl
    ) {
    }
}

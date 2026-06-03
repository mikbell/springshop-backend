package com.michelecampanello.springshop.domains.products.repository;

import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.products.dto.ProductSearchCriteria;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecifications {

    public static Specification<Product> filterByCriteria(ProductSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Cerca il termine nel Nome OPPURE nella Descrizione (case-insensitive)
            if (criteria.searchTerm() != null && !criteria.searchTerm().isBlank()) {
                String term = "%" + criteria.searchTerm().toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), term);
                Predicate descLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), term);
                predicates.add(criteriaBuilder.or(nameLike, descLike));
            }

            // 2. Filtro Prezzo Minimo
            if (criteria.minPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
            }

            // 3. Filtro Prezzo Massimo
            if (criteria.maxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
            }

            // 4. Filtro Solo Disponibili
            if (criteria.onlyAvailable() != null && criteria.onlyAvailable()) {
                predicates.add(criteriaBuilder.greaterThan(root.get("stockQuantity"), 0));
            }

            if (criteria.categoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), criteria.categoryId()));
            }

            if (criteria.categorySlug() != null && !criteria.categorySlug().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("slug"), criteria.categorySlug()));
            }

            // Uniamo tutti i filtri in AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

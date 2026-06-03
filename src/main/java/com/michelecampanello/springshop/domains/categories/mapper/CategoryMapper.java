package com.michelecampanello.springshop.domains.categories.mapper;

import com.michelecampanello.springshop.domains.categories.dto.CategoryRequest;
import com.michelecampanello.springshop.domains.categories.dto.CategoryResponse;
import com.michelecampanello.springshop.domains.categories.dto.CategorySummaryResponse;
import com.michelecampanello.springshop.domains.categories.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        Category category = new Category();
        updateEntity(category, request);
        return category;
    }

    public void updateEntity(Category category, CategoryRequest request) {
        category.setName(request.name());
        category.setDescription(request.description());
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public CategorySummaryResponse toSummary(Category category) {
        if (category == null) {
            return null;
        }
        return new CategorySummaryResponse(category.getId(), category.getName(), category.getSlug());
    }
}

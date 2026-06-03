package com.michelecampanello.springshop.domains.categories.model;

import com.michelecampanello.springshop.core.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 120, updatable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;
}

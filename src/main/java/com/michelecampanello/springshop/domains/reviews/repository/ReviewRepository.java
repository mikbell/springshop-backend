package com.michelecampanello.springshop.domains.reviews.repository;

import com.michelecampanello.springshop.domains.reviews.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByProductId(UUID productId, Pageable pageable);

    Optional<Review> findByProductIdAndUserId(UUID productId, UUID userId);

    boolean existsByProductIdAndUserId(UUID productId, UUID userId);

    long countByProductId(UUID productId);

    @Query("select avg(r.rating) from Review r where r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") UUID productId);
}

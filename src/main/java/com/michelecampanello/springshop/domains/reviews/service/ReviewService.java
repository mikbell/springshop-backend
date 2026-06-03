package com.michelecampanello.springshop.domains.reviews.service;

import com.michelecampanello.springshop.core.dto.PageResponse;
import com.michelecampanello.springshop.core.exceptions.DuplicateResourceException;
import com.michelecampanello.springshop.core.exceptions.ResourceNotFoundException;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import com.michelecampanello.springshop.domains.reviews.dto.ReviewRequest;
import com.michelecampanello.springshop.domains.reviews.dto.ReviewResponse;
import com.michelecampanello.springshop.domains.reviews.mapper.ReviewMapper;
import com.michelecampanello.springshop.domains.reviews.model.Review;
import com.michelecampanello.springshop.domains.reviews.repository.ReviewRepository;
import com.michelecampanello.springshop.domains.users.model.User;
import com.michelecampanello.springshop.domains.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(UUID productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Prodotto non trovato: " + productId);
        }
        return PageResponse.from(reviewRepository.findByProductId(productId, pageable).map(reviewMapper::toResponse));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "products", allEntries = true),
            @CacheEvict(cacheNames = "product", allEntries = true)
    })
    @Transactional
    public ReviewResponse createReview(UUID productId, UUID userId, ReviewRequest request) {
        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new DuplicateResourceException("Hai già recensito questo prodotto.");
        }

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato: " + productId));
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + userId));

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        reviewMapper.updateEntity(review, request);
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "products", allEntries = true),
            @CacheEvict(cacheNames = "product", allEntries = true)
    })
    @Transactional
    public ReviewResponse updateReview(UUID reviewId, User currentUser, ReviewRequest request) {
        Review review = getReviewOrThrow(reviewId);
        assertCanManage(review, currentUser);
        reviewMapper.updateEntity(review, request);
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "products", allEntries = true),
            @CacheEvict(cacheNames = "product", allEntries = true)
    })
    @Transactional
    public void deleteReview(UUID reviewId, User currentUser) {
        Review review = getReviewOrThrow(reviewId);
        assertCanManage(review, currentUser);
        reviewRepository.delete(review);
    }

    private Review getReviewOrThrow(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Recensione non trovata: " + reviewId));
    }

    private void assertCanManage(Review review, User currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !review.getUser().getId().equals(currentUser.getId())) {
            throw new AuthorizationDeniedException("Non autorizzato a modificare questa recensione", () -> false);
        }
    }
}

package com.michelecampanello.springshop.domains.reviews.service;

import com.michelecampanello.springshop.core.exceptions.DuplicateResourceException;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import com.michelecampanello.springshop.domains.reviews.dto.ReviewRequest;
import com.michelecampanello.springshop.domains.reviews.mapper.ReviewMapper;
import com.michelecampanello.springshop.domains.reviews.model.Review;
import com.michelecampanello.springshop.domains.reviews.repository.ReviewRepository;
import com.michelecampanello.springshop.domains.users.model.User;
import com.michelecampanello.springshop.domains.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReviewRejectsDuplicateProductReviewBySameUser() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ReviewRequest request = new ReviewRequest(5, "Ottimo");
        when(reviewRepository.existsByProductIdAndUserId(productId, userId)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(productId, userId, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReviewRejectsNonOwnerNonAdmin() {
        UUID reviewId = UUID.randomUUID();
        User owner = user(UUID.randomUUID(), User.Role.CUSTOMER);
        User otherUser = user(UUID.randomUUID(), User.Role.CUSTOMER);
        Review review = review(owner);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.updateReview(reviewId, otherUser, new ReviewRequest(4, "Ok")))
                .isInstanceOf(AuthorizationDeniedException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteReviewAllowsAdmin() {
        UUID reviewId = UUID.randomUUID();
        Review review = review(user(UUID.randomUUID(), User.Role.CUSTOMER));
        User admin = user(UUID.randomUUID(), User.Role.ADMIN);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        reviewService.deleteReview(reviewId, admin);

        verify(reviewRepository).delete(review);
    }

    private Review review(User owner) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        Review review = new Review();
        review.setProduct(product);
        review.setUser(owner);
        review.setRating(5);
        review.setComment("Ottimo");
        return review;
    }

    private User user(UUID id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setEmail(id + "@example.com");
        user.setPassword("secret");
        user.setRole(role);
        return user;
    }
}

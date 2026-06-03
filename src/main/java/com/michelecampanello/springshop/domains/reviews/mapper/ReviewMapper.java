package com.michelecampanello.springshop.domains.reviews.mapper;

import com.michelecampanello.springshop.domains.reviews.dto.ReviewRequest;
import com.michelecampanello.springshop.domains.reviews.dto.ReviewResponse;
import com.michelecampanello.springshop.domains.reviews.model.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getUser().getId(),
                review.getUser().getFirstName() + " " + review.getUser().getLastName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    public void updateEntity(Review review, ReviewRequest request) {
        review.setRating(request.rating());
        review.setComment(request.comment());
    }
}

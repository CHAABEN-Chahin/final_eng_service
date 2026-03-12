package com.nursery.service;

import com.nursery.model.Review;
import com.nursery.repository.ReviewRepository;
import com.nursery.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final NurseryService nurseryService;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         NurseryService nurseryService,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.nurseryService = nurseryService;
        this.userRepository = userRepository;
    }

    public Map<String, Object> updateReview(String reviewId, String parentId, Integer rating, String comment) {
        Map<String, Object> result = new HashMap<>();
        Optional<Review> opt = reviewRepository.findById(reviewId);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Review not found");
            return result;
        }
        Review review = opt.get();
        if (!review.getParentId().equals(parentId)) {
            result.put("success", false);
            result.put("message", "Unauthorized");
            return result;
        }
        if (rating != null) review.setRating(rating);
        if (comment != null) review.setComment(comment);
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);

        nurseryService.updateNurseryRating(review.getNurseryId());

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", review.getId());
        dto.put("nursery_id", review.getNurseryId());
        dto.put("parent_id", review.getParentId());
        dto.put("rating", review.getRating());
        dto.put("comment", review.getComment());
        dto.put("created_at", review.getCreatedAt());
        userRepository.findById(parentId).ifPresent(u -> dto.put("parent_name", u.getName()));

        result.put("success", true);
        result.put("review", dto);
        return result;
    }

    public Map<String, Object> deleteReview(String reviewId, String parentId) {
        Map<String, Object> result = new HashMap<>();
        Optional<Review> opt = reviewRepository.findById(reviewId);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Review not found");
            return result;
        }
        Review review = opt.get();
        if (!review.getParentId().equals(parentId)) {
            result.put("success", false);
            result.put("message", "Unauthorized");
            return result;
        }
        String nurseryId = review.getNurseryId();
        reviewRepository.deleteById(reviewId);
        nurseryService.updateNurseryRating(nurseryId);
        result.put("success", true);
        return result;
    }
}

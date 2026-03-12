package com.nursery.controller;

import com.nursery.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> updateReview(@PathVariable String reviewId,
                                                             @RequestBody Map<String, Object> body) {
        String parentId = (String) body.get("parentId");
        Integer rating = body.get("rating") instanceof Number ? ((Number) body.get("rating")).intValue() : null;
        String comment = (String) body.get("comment");
        Map<String, Object> result = reviewService.updateReview(reviewId, parentId, rating, comment);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 400).body(result);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable String reviewId,
                                                             @RequestBody Map<String, Object> body) {
        String parentId = (String) body.get("parentId");
        Map<String, Object> result = reviewService.deleteReview(reviewId, parentId);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 400).body(result);
    }
}

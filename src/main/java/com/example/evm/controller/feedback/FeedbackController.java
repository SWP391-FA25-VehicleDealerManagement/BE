package com.example.evm.controller.feedback;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.feedback.FeedbackInfo;
import com.example.evm.dto.feedback.FeedbackRequest;
import com.example.evm.service.feedback.FeedbackService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','EVM_STAFF','DEALER_MANAGER','DEALER_STAFF')")
    public ResponseEntity<ApiResponse<List<FeedbackInfo>>> getAllFeedbacks() {
        List<FeedbackInfo> feedbacks = feedbackService.getAllFeedbacks();
        return ResponseEntity.ok(new ApiResponse<>(true, "Feedbacks retrieved successfully", feedbacks));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','EVM_STAFF','DEALER_MANAGER','DEALER_STAFF')")
    public ResponseEntity<ApiResponse<FeedbackInfo>> getFeedbackById(@PathVariable Long id) {
        FeedbackInfo feedback = feedbackService.getFeedbackById(id);
        return feedback != null
                ? ResponseEntity.ok(new ApiResponse<>(true, "Feedback retrieved successfully", feedback))
                : ResponseEntity.badRequest().body(new ApiResponse<>(false, "Feedback not found", null));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','EVM_STAFF','DEALER_MANAGER','DEALER_STAFF')")
    public ResponseEntity<ApiResponse<FeedbackInfo>> createFeedback(@RequestBody FeedbackRequest request) {
        try {
            FeedbackInfo created = feedbackService.createFeedback(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Feedback created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','EVM_STAFF','DEALER_MANAGER','DEALER_STAFF')")
    public ResponseEntity<ApiResponse<FeedbackInfo>> updateFeedback(@PathVariable Long id, @RequestBody FeedbackRequest request) {
        try {
            FeedbackInfo updated = feedbackService.updateFeedback(id, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Feedback updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable Long id) {
        try {
            feedbackService.deleteFeedback(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Feedback deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}

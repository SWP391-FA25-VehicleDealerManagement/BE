package com.example.evm.service.feedback;

import java.util.List;

import com.example.evm.dto.feedback.FeedbackInfo;
import com.example.evm.dto.feedback.FeedbackRequest;
import com.example.evm.entity.feedback.Feedback;


public interface FeedbackService {
    List<FeedbackInfo> getAllFeedbacks();
    FeedbackInfo getFeedbackById(Long id);
    FeedbackInfo createFeedback(FeedbackRequest request);
    FeedbackInfo updateFeedback(Long id, FeedbackRequest request);
    void deleteFeedback(Long id);
} 

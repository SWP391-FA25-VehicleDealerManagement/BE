package com.example.evm.service.feedback;

import java.util.List;

import com.example.evm.entity.feedback.Feedback;

public interface FeedbackService {
    List<Feedback> getAllFeedbacks();
    Feedback getFeedbackById(Long id);
    Feedback createFeedback(Feedback feedback);
    Feedback updateFeedback( Feedback feedback);
    void deleteFeedback(Long id);
} 

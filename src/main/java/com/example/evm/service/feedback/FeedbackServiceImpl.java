package com.example.evm.service.feedback;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.evm.entity.feedback.Feedback;
import com.example.evm.entity.testDrive.TestDrive;
import com.example.evm.repository.feedback.FeedbackRepository;
import com.example.evm.repository.testDrive.TestDriveRepository;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private TestDriveRepository testDriveRepository;

    @Override
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @Override
    public Feedback getFeedbackById(Long id) {
        return feedbackRepository.findById(id).orElse(null);
    }

    @Override
    public Feedback createFeedback(Feedback feedback) {
        // Cho phép test mà không cần testDriveId
        if (feedback.getTestDrive() != null && feedback.getTestDrive().getTestDriveId() != null) {
            Long testDriveId = feedback.getTestDrive().getTestDriveId();
            Optional<TestDrive> testDriveOpt = testDriveRepository.findById(testDriveId);
            if (testDriveOpt.isEmpty()) {
                throw new IllegalArgumentException("Invalid TestDriveId: " + testDriveId);
            }
            feedback.setTestDrive(testDriveOpt.get());
        } else {
            feedback.setTestDrive(null);
        }

        return feedbackRepository.save(feedback);
    }

    @Override
    public Feedback updateFeedback(Feedback feedback) {
        if (feedback.getFeedbackId() == null || !feedbackRepository.existsById(feedback.getFeedbackId())) {
            throw new IllegalArgumentException("Feedback not found");
        }

        if (feedback.getTestDrive() != null && feedback.getTestDrive().getTestDriveId() != null) {
            Long testDriveId = feedback.getTestDrive().getTestDriveId();
            Optional<TestDrive> testDriveOpt = testDriveRepository.findById(testDriveId);
            if (testDriveOpt.isEmpty()) {
                throw new IllegalArgumentException("Invalid TestDriveId: " + testDriveId);
            }
            feedback.setTestDrive(testDriveOpt.get());
        } else {
            feedback.setTestDrive(null);
        }

        return feedbackRepository.save(feedback);
    }

    @Override
    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}

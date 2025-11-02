package com.example.evm.service.feedback;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.evm.dto.feedback.FeedbackInfo;
import com.example.evm.dto.feedback.FeedbackRequest;
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
    public List<FeedbackInfo> getAllFeedbacks() {
        return feedbackRepository.findAll().stream()
                .map(this::toFeedbackInfo)
                .collect(Collectors.toList());
    }

    @Override
    public FeedbackInfo getFeedbackById(Long id) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        return feedback.map(this::toFeedbackInfo).orElse(null);
    }

    private FeedbackInfo toFeedbackInfo(Feedback feedback) {
        FeedbackInfo info = new FeedbackInfo();
        info.setFeedbackId(feedback.getFeedbackId());
        info.setDescription(feedback.getDescription());
        info.setFeedbackType(feedback.getFeedbackType());
        info.setContent(feedback.getContent());
        info.setStatus(feedback.getStatus());
        
        if (feedback.getTestDrive() != null) {
            TestDrive td = feedback.getTestDrive();
            FeedbackInfo.TestDriveInfo testDriveInfo = new FeedbackInfo.TestDriveInfo(
                    td.getTestDriveId(),
                    td.getScheduledDate(),
                    td.getStatus(),
                    td.getNotes(),
                    td.getAssignedBy(),
                    td.getCreatedDate()
            );
            info.setTestDrive(testDriveInfo);
        }
        
        return info;
    }

    @Override
    public FeedbackInfo createFeedback(FeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setDescription(request.getDescription());
        feedback.setFeedbackType(request.getFeedbackType());
        feedback.setContent(request.getContent());
        feedback.setStatus(request.getStatus());
        
        if (request.getTestDriveId() == null) {
            throw new IllegalArgumentException("testDriveId is required");
        }
        TestDrive testDrive = testDriveRepository.findById(request.getTestDriveId())
            .orElseThrow(() -> new IllegalArgumentException("TestDrive not found: " + request.getTestDriveId()));
        feedback.setTestDrive(testDrive);
        
        Feedback saved = feedbackRepository.save(feedback);
        return toFeedbackInfo(saved);
    }

    @Override
    public FeedbackInfo updateFeedback(Long id, FeedbackRequest request) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
                if (request.getDescription() != null) {
                    feedback.setDescription(request.getDescription());
                }
                if (request.getFeedbackType() != null) {
                    feedback.setFeedbackType(request.getFeedbackType());
                }
                if (request.getContent() != null) {
                    feedback.setContent(request.getContent());
                }
                if (request.getStatus() != null) {
                    feedback.setStatus(request.getStatus());
                }
                if (request.getTestDriveId() != null) {
                    TestDrive testDrive = testDriveRepository.findById(request.getTestDriveId())
                        .orElseThrow(() -> new IllegalArgumentException("TestDrive not found: " + request.getTestDriveId()));
                    feedback.setTestDrive(testDrive);
                }
        Feedback updated = feedbackRepository.save(feedback);
        return toFeedbackInfo(updated);
    }

    @Override
    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}

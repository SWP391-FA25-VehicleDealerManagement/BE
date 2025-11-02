package com.example.evm.dto.feedback;

import lombok.Data;

@Data
public class FeedbackRequest {
    private String description;
    private String feedbackType;
    private String content;
    private String status;
    private Long testDriveId;
}

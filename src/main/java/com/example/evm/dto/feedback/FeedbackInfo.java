package com.example.evm.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackInfo {
 private Long feedbackId;
    private Long testdriveId; 
    private String description;
    private String feedbackType;
    private String content;
    private String status;
}

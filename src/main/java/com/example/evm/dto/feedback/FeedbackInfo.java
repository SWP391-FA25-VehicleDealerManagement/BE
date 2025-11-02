package com.example.evm.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackInfo {

    private Long feedbackId;
    private String description;
    private String feedbackType;
    private String content;
    private String status;

    private TestDriveInfo testDrive;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestDriveInfo {
        private Long testDriveId;
        private LocalDateTime scheduledDate;     
        private String status;                 
        private String notes;                   
        private String assignedBy;               
        private LocalDateTime createdDate;      
        
    }
}
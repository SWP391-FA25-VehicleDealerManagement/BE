package com.example.evm.entity.feedback;

import com.example.evm.entity.testDrive.TestDrive;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @JsonBackReference // Prevent lazy loading serialization error
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "testdrive_id", nullable = true)
    private TestDrive testDrive;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "feedbackType", length = 255)
    private String feedbackType;

    @Column(name = "content", length = 255)
    private String content;

    @Column(name = "status", length = 255)
    private String status;
    
    // Helper method để expose testDriveId mà không trigger lazy loading
    public Long getTestDriveId() {
        return testDrive != null ? testDrive.getTestDriveId() : null;
    }
}

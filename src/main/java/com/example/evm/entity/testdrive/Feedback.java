package com.example.evm.entity.testdrive;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Feedback {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)

@Column(name = "feedback_id")
private int feedbackid;

@Column(name = "testdrive_id")
private int testdriveid;

@Column(name = "description")
private String description;

@Column(name = "feedbackType")
private String feedbackType;

@Column(name = "content")
private String content;

@Column(name = "status")
private String status;

public int getFeedbackid() {
    return feedbackid;
}

public void setFeedbackid(int feedbackid) {
    this.feedbackid = feedbackid;
}

public int getTestdriveid() {
    return testdriveid;
}

public void setTestdriveid(int testdriveid) {
    this.testdriveid = testdriveid;
}

public String getDescription() {
    return description;
}

public void setDescription(String description) {
    this.description = description;
}

public String getFeedbackType() {
    return feedbackType;
}

public void setFeedbackType(String feedbackType) {
    this.feedbackType = feedbackType;
}

public String getContent() {
    return content;
}

public void setContent(String content) {
    this.content = content;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}
}

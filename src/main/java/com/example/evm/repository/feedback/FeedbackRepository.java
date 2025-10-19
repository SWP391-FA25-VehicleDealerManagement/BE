package com.example.evm.repository.feedback;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evm.entity.feedback.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback,Long>{

}

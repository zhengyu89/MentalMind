package com.example.MentalMind.repository;

import com.example.MentalMind.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUserId(Long userId);
    List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Feedback> findByTypeAndStatus(String type, String status);
}

package com.example.MentalMind.repository;

import com.example.MentalMind.model.SelfAssessmentResult;
import com.example.MentalMind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SelfAssessmentRepository extends JpaRepository<SelfAssessmentResult, Long> {

    // Find the most recent assessment result for a user
    Optional<SelfAssessmentResult> findTopByUserOrderByCompletedAtDesc(User user);

    // Find all assessments by user (for history if needed later)
    List<SelfAssessmentResult> findByUserOrderByCompletedAtDesc(User user);

    // Check if user has taken assessment today
    @Query("SELECT COUNT(s) > 0 FROM SelfAssessmentResult s WHERE s.user = :user AND s.completedAt >= :startOfDay")
    boolean hasCompletedToday(User user, LocalDateTime startOfDay);
}

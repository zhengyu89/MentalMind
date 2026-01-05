package com.example.MentalMind.repository;

import com.example.MentalMind.model.CounselorResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounselorResponseRepository extends JpaRepository<CounselorResponse, Long> {
    List<CounselorResponse> findByFeedbackId(Long feedbackId);
    List<CounselorResponse> findByCounselorId(Long counselorId);
}

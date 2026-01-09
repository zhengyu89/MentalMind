package com.example.MentalMind.repository;

import com.example.MentalMind.model.LearningModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LearningModuleRepository extends JpaRepository<LearningModule, Long> {
    List<LearningModule> findByIsActiveTrue();
    List<LearningModule> findByCreatedBy(Long createdBy);
}

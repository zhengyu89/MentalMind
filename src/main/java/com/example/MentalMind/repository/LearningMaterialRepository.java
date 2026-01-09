package com.example.MentalMind.repository;

import com.example.MentalMind.model.LearningMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Long> {
    List<LearningMaterial> findByModuleIdAndIsActiveTrue(Long moduleId);
    List<LearningMaterial> findByModuleId(Long moduleId);
    List<LearningMaterial> findByCreatedBy(Long createdBy);
}

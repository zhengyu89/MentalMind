package com.example.MentalMind.repository;

import com.example.MentalMind.model.StudentMaterialProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentMaterialProgressRepository extends JpaRepository<StudentMaterialProgress, Long> {
    List<StudentMaterialProgress> findByStudentId(Long studentId);
    Optional<StudentMaterialProgress> findByStudentIdAndMaterialId(Long studentId, Long materialId);
    List<StudentMaterialProgress> findByMaterialId(Long materialId);
    List<StudentMaterialProgress> findByStudentIdAndIsCompletedTrue(Long studentId);
}

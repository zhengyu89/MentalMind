package com.example.MentalMind.service;

import com.example.MentalMind.model.StudentMaterialProgress;
import com.example.MentalMind.model.LearningMaterial;
import com.example.MentalMind.model.LearningModule;
import com.example.MentalMind.repository.StudentMaterialProgressRepository;
import com.example.MentalMind.repository.LearningMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LearningProgressService {

    @Autowired
    private StudentMaterialProgressRepository progressRepository;

    @Autowired
    private LearningMaterialRepository materialRepository;

    /**
     * Mark a material as completed for a student
     */
    @Transactional
    public StudentMaterialProgress markAsCompleted(Long studentId, Long materialId) {
        Optional<StudentMaterialProgress> existingProgress = progressRepository
                .findByStudentIdAndMaterialId(studentId, materialId);

        StudentMaterialProgress progress;
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
            if (!progress.getIsCompleted()) {
                progress.setIsCompleted(true);
                progress.setCompletedAt(LocalDateTime.now());
            }
        } else {
            progress = new StudentMaterialProgress();
            progress.setStudentId(studentId);
            
            Optional<LearningMaterial> material = materialRepository.findById(materialId);
            if (material.isEmpty()) {
                throw new RuntimeException("Material not found");
            }
            progress.setMaterial(material.get());
            progress.setIsCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        return progressRepository.save(progress);
    }

    /**
     * Mark a material as not completed (undo)
     */
    @Transactional
    public void markAsIncomplete(Long studentId, Long materialId) {
        Optional<StudentMaterialProgress> existingProgress = progressRepository
                .findByStudentIdAndMaterialId(studentId, materialId);

        if (existingProgress.isPresent()) {
            StudentMaterialProgress progress = existingProgress.get();
            progress.setIsCompleted(false);
            progress.setCompletedAt(null);
            progressRepository.save(progress);
        }
    }

    /**
     * Check if a material is completed by a student
     */
    public boolean isMaterialCompleted(Long studentId, Long materialId) {
        Optional<StudentMaterialProgress> progress = progressRepository
                .findByStudentIdAndMaterialId(studentId, materialId);
        return progress.isPresent() && progress.get().getIsCompleted();
    }

    /**
     * Get all completed material IDs for a student
     */
    public Set<Long> getCompletedMaterialIds(Long studentId) {
        return progressRepository.findByStudentIdAndIsCompletedTrue(studentId)
                .stream()
                .map(p -> p.getMaterial().getId())
                .collect(Collectors.toSet());
    }

    /**
     * Get all completed material IDs for a student within a specific module
     */
    public Set<Long> getCompletedMaterialIdsForModule(Long studentId, Long moduleId) {
        return progressRepository.findByStudentIdAndIsCompletedTrue(studentId)
                .stream()
                .filter(p -> p.getMaterial().getModule().getId().equals(moduleId))
                .map(p -> p.getMaterial().getId())
                .collect(Collectors.toSet());
    }

    /**
     * Get progress percentage for a module
     */
    public int getModuleProgressPercentage(Long studentId, LearningModule module) {
        List<LearningMaterial> activeMaterials = module.getMaterials().stream()
                .filter(LearningMaterial::getIsActive)
                .toList();

        if (activeMaterials.isEmpty()) {
            return 0;
        }

        Set<Long> completedIds = getCompletedMaterialIdsForModule(studentId, module.getId());
        int completedCount = (int) activeMaterials.stream()
                .filter(m -> completedIds.contains(m.getId()))
                .count();

        return (int) Math.round((double) completedCount / activeMaterials.size() * 100);
    }

    /**
     * Get progress statistics for all modules
     */
    public Map<Long, Integer> getAllModulesProgress(Long studentId, List<LearningModule> modules) {
        Map<Long, Integer> progressMap = new HashMap<>();
        for (LearningModule module : modules) {
            progressMap.put(module.getId(), getModuleProgressPercentage(studentId, module));
        }
        return progressMap;
    }

    /**
     * Get the progress record for a student and material
     */
    public Optional<StudentMaterialProgress> getProgress(Long studentId, Long materialId) {
        return progressRepository.findByStudentIdAndMaterialId(studentId, materialId);
    }

    /**
     * Get all progress records for a student
     */
    public List<StudentMaterialProgress> getAllProgressForStudent(Long studentId) {
        return progressRepository.findByStudentId(studentId);
    }

    /**
     * Get completed materials count for a student
     */
    public long getCompletedMaterialsCount(Long studentId) {
        return progressRepository.findByStudentIdAndIsCompletedTrue(studentId).size();
    }
}

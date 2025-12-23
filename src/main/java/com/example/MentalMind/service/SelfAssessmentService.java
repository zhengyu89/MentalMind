package com.example.MentalMind.service;

import com.example.MentalMind.model.SelfAssessmentResult;
import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.SelfAssessmentRepository;
import com.example.MentalMind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SelfAssessmentService {

    @Autowired
    private SelfAssessmentRepository selfAssessmentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Calculate stress level based on PSS-10 score
     */
    public String calculateStressLevel(int score) {
        if (score <= 13) {
            return "LOW";
        } else if (score <= 26) {
            return "MODERATE";
        } else {
            return "HIGH";
        }
    }

    /**
     * Save a self-assessment result
     */
    @Transactional
    public SelfAssessmentResult saveResult(Long userId, int score) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String stressLevel = calculateStressLevel(score);
        SelfAssessmentResult result = new SelfAssessmentResult(user, score, stressLevel);
        return selfAssessmentRepository.save(result);
    }

    /**
     * Get the latest assessment result for a user
     */
    public Optional<SelfAssessmentResult> getLatestResult(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return selfAssessmentRepository.findTopByUserOrderByCompletedAtDesc(user);
    }

    /**
     * Check if user has already completed assessment today
     */
    public boolean hasCompletedToday(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return selfAssessmentRepository.hasCompletedToday(user, startOfDay);
    }

    /**
     * Get recommendations based on stress level
     */
    public Map<String, Object> getRecommendations(String stressLevel) {
        Map<String, Object> recommendations = new HashMap<>();
        List<String> tips = new ArrayList<>();
        String description;
        String color;

        switch (stressLevel) {
            case "LOW":
                description = "Great job! You're managing stress well.";
                color = "bg-green-500";
                tips.add("Continue your current healthy habits");
                tips.add("Practice daily gratitude journaling");
                tips.add("Maintain your social connections");
                tips.add("Keep up regular exercise routines");
                break;
            case "MODERATE":
                description = "You're experiencing some stress. Consider trying relaxation techniques.";
                color = "bg-yellow-500";
                tips.add("Try deep breathing exercises daily");
                tips.add("Take regular breaks from work/study");
                tips.add("Talk to friends or family about your feelings");
                tips.add("Explore our Learning modules on stress management");
                tips.add("Practice progressive muscle relaxation");
                break;
            case "HIGH":
            default:
                description = "Your stress levels are elevated. We recommend seeking support.";
                color = "bg-red-500";
                tips.add("Schedule a counseling appointment soon");
                tips.add("Use the Emergency Help resources if needed");
                tips.add("Practice grounding techniques from our Resources");
                tips.add("Prioritize self-care activities daily");
                tips.add("Consider talking to a trusted person about your stress");
                tips.add("Try to identify and reduce major stressors");
                break;
        }

        recommendations.put("description", description);
        recommendations.put("color", color);
        recommendations.put("tips", tips);
        recommendations.put("stressLevel", stressLevel);

        return recommendations;
    }
}

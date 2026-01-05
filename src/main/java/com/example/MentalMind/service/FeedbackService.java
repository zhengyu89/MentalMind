package com.example.MentalMind.service;

import com.example.MentalMind.model.Feedback;
import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    /**
     * Submit feedback from a user
     */
    public Feedback submitFeedback(User user, String type, String subject, String details) {
        Feedback feedback = new Feedback(user, type, subject, details);
        return feedbackRepository.save(feedback);
    }

    /**
     * Get all feedback submissions by a user
     */
    public List<Feedback> getUserFeedback(Long userId) {
        return feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get recent feedback submissions (last 3)
     */
    public List<Feedback> getRecentUserFeedback(Long userId) {
        List<Feedback> feedbackList = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return feedbackList.stream().limit(3).toList();
    }

    /**
     * Get feedback by id
     */
    public Optional<Feedback> getFeedbackById(Long id) {
        return feedbackRepository.findById(id);
    }

    /**
     * Update feedback status
     */
    public Feedback updateFeedbackStatus(Long id, String status) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            Feedback f = feedback.get();
            f.setStatus(status);
            return feedbackRepository.save(f);
        }
        return null;
    }

    /**
     * Get all feedback by type and status (for admin use)
     */
    public List<Feedback> getFeedbackByTypeAndStatus(String type, String status) {
        return feedbackRepository.findByTypeAndStatus(type, status);
    }

    /**
     * Delete feedback
     */
    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }

    /**
     * Get recent feedback across all users (most recent first)
     */
    public java.util.List<Feedback> getRecentFeedback(int limit) {
        java.util.List<Feedback> all = feedbackRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        return all.stream().limit(limit).toList();
    }
}

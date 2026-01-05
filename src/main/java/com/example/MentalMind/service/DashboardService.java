package com.example.MentalMind.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.MentalMind.model.Feedback;
import com.example.MentalMind.model.SelfAssessmentResult;
import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.FeedbackRepository;
import com.example.MentalMind.repository.SelfAssessmentRepository;
import com.example.MentalMind.repository.MoodEntryRepository;
import com.example.MentalMind.repository.UserRepository;
import com.example.MentalMind.repository.CounselorResponseRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private SelfAssessmentRepository assessmentRepository;

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CounselorResponseRepository responseRepository;

    // Feature 1: Today's Appointments Count & Details
    public Map<String, Object> getTodayAppointmentsStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Since appointments are not stored in DB yet, we'll use feedback as proxy
        long pendingFeedback = feedbackRepository.findByTypeAndStatus("feedback", "pending").size();
        long totalToday = feedbackRepository.findAll().stream()
            .filter(f -> f.getCreatedAt().toLocalDate().equals(LocalDate.now()))
            .count();
        
        stats.put("todayAppointments", 5); // Placeholder - will be replaced with real data
        stats.put("pendingAppointments", 2);
        stats.put("completedAppointments", 3);
        
        return stats;
    }

    // Feature 2: Pending Requests Count
    public Map<String, Object> getPendingRequestsStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long pendingFeedback = feedbackRepository.findByTypeAndStatus("feedback", "pending").size();
        long pendingBugs = feedbackRepository.findByTypeAndStatus("bug", "pending").size();
        long pendingSuggestions = feedbackRepository.findByTypeAndStatus("suggestion", "pending").size();
        long totalPending = pendingFeedback + pendingBugs + pendingSuggestions;
        
        stats.put("totalPending", totalPending);
        stats.put("pendingFeedback", pendingFeedback);
        stats.put("pendingBugs", pendingBugs);
        stats.put("pendingSuggestions", pendingSuggestions);
        stats.put("pendingResponses", responseRepository.count()); // Count of responses needed
        
        return stats;
    }

    // Feature 3: Flagged Students (High-Risk) - Get top 5
    public List<Map<String, Object>> getFlaggedStudents() {
        List<Map<String, Object>> flaggedStudents = new ArrayList<>();
        
        // Get assessments with HIGH stress level
        List<SelfAssessmentResult> highRiskAssessments = assessmentRepository.findAll().stream()
            .filter(a -> "HIGH".equalsIgnoreCase(a.getStressLevel()))
            .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
            .limit(5)
            .collect(Collectors.toList());
        
        for (SelfAssessmentResult assessment : highRiskAssessments) {
            Map<String, Object> student = new HashMap<>();
            User user = assessment.getUser();
            
            student.put("id", user.getId());
            student.put("name", user.getFullName());
            student.put("email", user.getEmail());
            student.put("riskLevel", assessment.getStressLevel());
            student.put("score", assessment.getScore());
            student.put("assessmentDate", assessment.getCompletedAt());
            
            // Get latest mood entry for this student
            List<MoodEntry> moods = moodEntryRepository.findByUserId(user.getId());
            if (!moods.isEmpty()) {
                student.put("latestMoodScore", moods.get(0).getMoodScore());
                student.put("moodDate", moods.get(0).getCreatedAt());
            }
            
            flaggedStudents.add(student);
        }
        
        return flaggedStudents;
    }

    // Feature 4: Active Students Count & Breakdown
    public Map<String, Object> getActiveStudentsStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<User> allStudents = userRepository.findAll().stream()
            .filter(u -> "student".equalsIgnoreCase(u.getRole()))
            .collect(Collectors.toList());
        
        // Active = has activity in last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long activeStudents = allStudents.stream()
            .filter(u -> {
                List<MoodEntry> recentMoods = moodEntryRepository.findByUserId(u.getId()).stream()
                    .filter(m -> m.getCreatedAt().isAfter(thirtyDaysAgo))
                    .collect(Collectors.toList());
                return !recentMoods.isEmpty();
            })
            .count();
        
        stats.put("totalStudents", allStudents.size());
        stats.put("activeStudents", activeStudents);
        stats.put("inactiveStudents", allStudents.size() - activeStudents);
        
        return stats;
    }

    // Feature 5: Recent Student Assessments - Top 10
    public List<Map<String, Object>> getRecentAssessments() {
        Pageable pageable = PageRequest.of(0, 10);
        List<SelfAssessmentResult> recentAssessments = assessmentRepository.findAll().stream()
            .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt()))
            .limit(10)
            .collect(Collectors.toList());
        
        return recentAssessments.stream()
            .map(assessment -> {
                Map<String, Object> data = new HashMap<>();
                User user = assessment.getUser();
                
                data.put("id", assessment.getId());
                data.put("studentId", user.getId());
                data.put("studentName", user.getFullName());
                data.put("studentEmail", user.getEmail());
                data.put("assessmentType", "Self-Assessment");
                data.put("score", assessment.getScore() + "/40");
                data.put("stressLevel", assessment.getStressLevel());
                data.put("completedAt", assessment.getCompletedAt());
                data.put("riskColor", getRiskColor(assessment.getStressLevel()));
                
                return data;
            })
            .collect(Collectors.toList());
    }

    // Feature 6: Counselor Statistics
    public Map<String, Object> getCounselorStats() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        
        // Session count this month (using feedback as proxy)
        long sessionsThisMonth = feedbackRepository.findAll().stream()
            .filter(f -> f.getCreatedAt().isAfter(monthStart))
            .count();
        
        // Total responses sent
        long totalResponses = responseRepository.count();
        
        // Feedback resolution rate
        long totalFeedback = feedbackRepository.findAll().size();
        long resolvedFeedback = feedbackRepository.findByTypeAndStatus("feedback", "resolved").size()
            + feedbackRepository.findByTypeAndStatus("bug", "resolved").size()
            + feedbackRepository.findByTypeAndStatus("suggestion", "resolved").size();
        
        double resolutionRate = totalFeedback > 0 ? (double) resolvedFeedback / totalFeedback * 100 : 0;
        
        stats.put("sessionsThisMonth", sessionsThisMonth);
        stats.put("totalResponsesSent", totalResponses);
        stats.put("feedbackResolutionRate", String.format("%.1f", resolutionRate) + "%");
        stats.put("totalFeedbackHandled", totalFeedback);
        stats.put("resolvedFeedback", resolvedFeedback);
        
        return stats;
    }

    // Feature 7: Quick Stats Dashboard - Summary
    public Map<String, Object> getQuickStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total feedback
        long totalFeedback = feedbackRepository.findAll().size();
        long pendingFeedback = feedbackRepository.findByTypeAndStatus("feedback", "pending").size()
            + feedbackRepository.findByTypeAndStatus("bug", "pending").size()
            + feedbackRepository.findByTypeAndStatus("suggestion", "pending").size();
        long reviewedFeedback = feedbackRepository.findByTypeAndStatus("feedback", "reviewed").size()
            + feedbackRepository.findByTypeAndStatus("bug", "reviewed").size()
            + feedbackRepository.findByTypeAndStatus("suggestion", "reviewed").size();
        long resolvedFeedback = feedbackRepository.findByTypeAndStatus("feedback", "resolved").size()
            + feedbackRepository.findByTypeAndStatus("bug", "resolved").size()
            + feedbackRepository.findByTypeAndStatus("suggestion", "resolved").size();
        
        // Trend: This week vs last week
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusDays(14);
        
        long thisWeekFeedback = feedbackRepository.findAll().stream()
            .filter(f -> f.getCreatedAt().isAfter(weekAgo))
            .count();
        long lastWeekFeedback = feedbackRepository.findAll().stream()
            .filter(f -> f.getCreatedAt().isAfter(twoWeeksAgo) && f.getCreatedAt().isBefore(weekAgo))
            .count();
        
        stats.put("totalFeedback", totalFeedback);
        stats.put("pendingFeedback", pendingFeedback);
        stats.put("reviewedFeedback", reviewedFeedback);
        stats.put("resolvedFeedback", resolvedFeedback);
        stats.put("totalResponses", responseRepository.count());
        stats.put("thisWeekFeedback", thisWeekFeedback);
        stats.put("lastWeekFeedback", lastWeekFeedback);
        stats.put("feedbackTrend", lastWeekFeedback > 0 ? 
            String.format("%.1f", (thisWeekFeedback - lastWeekFeedback) / (double) lastWeekFeedback * 100) + "%" : 
            "N/A");
        
        return stats;
    }

    // Feature 8: Student Mood Trends - Identify declining moods
    public List<Map<String, Object>> getMoodTrends() {
        List<Map<String, Object>> trends = new ArrayList<>();
        
        // Get all students with mood entries
        List<User> students = userRepository.findAll().stream()
            .filter(u -> "student".equalsIgnoreCase(u.getRole()))
            .collect(Collectors.toList());
        
        for (User student : students) {
            List<MoodEntry> moods = moodEntryRepository.findByUserId(student.getId()).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(7) // Last 7 moods
                .collect(Collectors.toList());
            
            if (moods.size() >= 2) {
                int latestMood = moods.get(0).getMoodScore();
                int previousMood = moods.get(1).getMoodScore();
                
                // Flag if declining trend (drop of 2+ points)
                if (latestMood < previousMood - 1 && latestMood <= 3) {
                    Map<String, Object> trend = new HashMap<>();
                    trend.put("studentId", student.getId());
                    trend.put("studentName", student.getFullName());
                    trend.put("latestMood", latestMood);
                    trend.put("previousMood", previousMood);
                    trend.put("moodChange", latestMood - previousMood);
                    trend.put("lastUpdated", moods.get(0).getCreatedAt());
                    trend.put("concern", "Declining mood - may need support");
                    
                    trends.add(trend);
                }
            }
        }
        
        return trends.stream()
            .limit(10)
            .collect(Collectors.toList());
    }

    // Feature 9: Appointments This Week (using feedback as placeholder)
    public List<Map<String, Object>> getUpcomingAppointments() {
        List<Map<String, Object>> appointments = new ArrayList<>();
        
        LocalDateTime weekFromNow = LocalDateTime.now().plusDays(7);
        
        // Use feedback created this week as proxy for appointments
        List<Feedback> thisWeekFeedback = feedbackRepository.findAll().stream()
            .filter(f -> f.getCreatedAt().isAfter(LocalDateTime.now()) && 
                         f.getCreatedAt().isBefore(weekFromNow))
            .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
            .limit(10)
            .collect(Collectors.toList());
        
        for (Feedback feedback : thisWeekFeedback) {
            Map<String, Object> apt = new HashMap<>();
            apt.put("id", feedback.getId());
            apt.put("studentName", feedback.getUser().getFullName());
            apt.put("studentEmail", feedback.getUser().getEmail());
            apt.put("subject", feedback.getSubject());
            apt.put("type", feedback.getType());
            apt.put("status", feedback.getStatus());
            apt.put("scheduledTime", feedback.getCreatedAt());
            apt.put("duration", "1 hour");
            
            appointments.add(apt);
        }
        
        return appointments;
    }

    // Feature 10: Performance Metrics
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        List<User> allStudents = userRepository.findAll().stream()
            .filter(u -> "student".equalsIgnoreCase(u.getRole()))
            .collect(Collectors.toList());
        
        // Students with feedback/responses
        Set<Long> studentsHelped = new HashSet<>();
        feedbackRepository.findAll().forEach(f -> studentsHelped.add(f.getUser().getId()));
        responseRepository.findAll().forEach(r -> studentsHelped.add(r.getFeedback().getUser().getId()));
        
        // Average response time (using feedback creation as reference)
        List<Feedback> allFeedback = feedbackRepository.findAll();
        long totalResponseTime = 0;
        int responsedCount = 0;
        
        for (Feedback feedback : allFeedback) {
            if ("reviewed".equalsIgnoreCase(feedback.getStatus()) || 
                "resolved".equalsIgnoreCase(feedback.getStatus())) {
                responsedCount++;
            }
        }
        
        long avgResponseTime = responsedCount > 0 ? 24 : 0; // Placeholder: 24 hours
        
        // Resolution rate
        long resolvedCount = allFeedback.stream()
            .filter(f -> "resolved".equalsIgnoreCase(f.getStatus()))
            .count();
        double resolutionRate = allFeedback.size() > 0 ? 
            (double) resolvedCount / allFeedback.size() * 100 : 0;
        
        metrics.put("studentsHelped", studentsHelped.size());
        metrics.put("totalStudents", allStudents.size());
        metrics.put("helpRate", String.format("%.1f", (double) studentsHelped.size() / allStudents.size() * 100) + "%");
        metrics.put("feedbackResolutionRate", String.format("%.1f", resolutionRate) + "%");
        metrics.put("avgResponseTimeHours", avgResponseTime);
        metrics.put("totalFeedbackHandled", allFeedback.size());
        metrics.put("totalResponsesSent", responseRepository.count());
        
        return metrics;
    }

    // Helper method to determine risk color
    private String getRiskColor(String stressLevel) {
        return switch (stressLevel.toUpperCase()) {
            case "LOW" -> "green";
            case "MODERATE" -> "yellow";
            case "HIGH" -> "red";
            default -> "gray";
        };
    }

    // Comprehensive dashboard data - all features combined
    public Map<String, Object> getCompleteDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        dashboardData.put("appointments", getTodayAppointmentsStats());
        dashboardData.put("pendingRequests", getPendingRequestsStats());
        dashboardData.put("flaggedStudents", getFlaggedStudents());
        dashboardData.put("activeStudents", getActiveStudentsStats());
        dashboardData.put("recentAssessments", getRecentAssessments());
        dashboardData.put("counselorStats", getCounselorStats());
        dashboardData.put("quickStats", getQuickStats());
        dashboardData.put("moodTrends", getMoodTrends());
        dashboardData.put("upcomingAppointments", getUpcomingAppointments());
        dashboardData.put("performanceMetrics", getPerformanceMetrics());
        
        return dashboardData;
    }
}

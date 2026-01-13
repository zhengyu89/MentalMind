package com.example.MentalMind.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.MentalMind.model.Appointment;
import com.example.MentalMind.model.Feedback;
import com.example.MentalMind.model.SelfAssessmentResult;
import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.AppointmentRepository;
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

    @Autowired
    private AppointmentRepository appointmentRepository;

    // Feature 1: Today's Appointments Count & Details
    public Map<String, Object> getTodayAppointmentsStats(Long counselorId) {
        Map<String, Object> stats = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        List<Appointment> appointments;
        if (counselorId != null) {
            Optional<User> counselor = userRepository.findById(counselorId);
            if (counselor.isEmpty()) {
                stats.put("todayAppointments", 0);
                stats.put("pendingAppointments", 0);
                stats.put("completedAppointments", 0);
                return stats;
            }
            appointments = appointmentRepository.findByCounselorOrderByAppointmentDateTimeAsc(counselor.get());
        } else {
            appointments = appointmentRepository.findAll();
        }

        List<Appointment> todayApts = appointments.stream()
            .filter(a -> !a.getAppointmentDateTime().isBefore(start) && !a.getAppointmentDateTime().isAfter(end))
            .collect(Collectors.toList());

        long todayAppointments = todayApts.size();
        long pendingAppointments = todayApts.stream()
            .filter(a -> "PENDING".equalsIgnoreCase(a.getStatus()))
            .count();
        long completedAppointments = todayApts.stream()
            .filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus()))
            .count();

        stats.put("todayAppointments", todayAppointments);
        stats.put("pendingAppointments", pendingAppointments);
        stats.put("completedAppointments", completedAppointments);

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
        Map<Long, SelfAssessmentResult> latestHighRiskByUser = new HashMap<>();

        assessmentRepository.findAll().stream()
            .filter(a -> "HIGH".equalsIgnoreCase(a.getStressLevel()))
            .filter(a -> a.getUser() != null && "student".equalsIgnoreCase(a.getUser().getRole()))
            .forEach(a -> {
                Long userId = a.getUser().getId();
                SelfAssessmentResult current = latestHighRiskByUser.get(userId);
                if (current == null || a.getCompletedAt().isAfter(current.getCompletedAt())) {
                    latestHighRiskByUser.put(userId, a);
                }
            });

        return latestHighRiskByUser.values().stream()
            .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
            .map(assessment -> {
                Map<String, Object> student = new HashMap<>();
                User user = assessment.getUser();

                student.put("id", user.getId());
                student.put("name", user.getFullName());
                student.put("email", user.getEmail());
                student.put("riskLevel", assessment.getStressLevel());
                student.put("score", assessment.getScore());
                student.put("assessmentDate", assessment.getCompletedAt());

                List<MoodEntry> moods = moodEntryRepository.findByUserId(user.getId());
                if (!moods.isEmpty()) {
                    student.put("latestMoodScore", moods.get(0).getMoodScore());
                    student.put("moodDate", moods.get(0).getCreatedAt());
                }

                return student;
            })
            .collect(Collectors.toList());
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
                data.put("score", assessment.getScore());
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
    public List<Map<String, Object>> getUpcomingAppointments(Long counselorId) {
        if (counselorId == null) {
            return List.of();
        }

        Optional<User> counselor = userRepository.findById(counselorId);
        if (counselor.isEmpty()) {
            return List.of();
        }

        LocalDate today = LocalDate.now();
        LocalDateTime windowStart = today.atStartOfDay();
        LocalDateTime windowEnd = today.plusDays(7).atTime(LocalTime.MAX);

        return appointmentRepository.findByCounselorOrderByAppointmentDateTimeAsc(counselor.get()).stream()
            .filter(a -> !a.getAppointmentDateTime().isBefore(windowStart) && !a.getAppointmentDateTime().isAfter(windowEnd))
            .filter(a -> "APPROVED".equalsIgnoreCase(a.getStatus()) || "PENDING".equalsIgnoreCase(a.getStatus()))
            .map(apt -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", apt.getId());
                data.put("studentName", apt.getStudent().getFullName());
                data.put("studentEmail", apt.getStudent().getEmail());
                data.put("subject", apt.getReason());
                data.put("type", "Counseling");
                data.put("status", apt.getStatus());
                data.put("scheduledTime", apt.getAppointmentDateTime());
                return data;
            })
            .collect(Collectors.toList());
    }

    public Map<String, Object> getTotalStudents() {
        long total = userRepository.findByRole("student").size();
        return Map.of("totalStudents", total);
    }

    public Map<String, Object> getAssessmentsTaken() {
        long total = assessmentRepository.count();
        return Map.of("totalAssessments", total);
    }

    public Map<String, Object> getAssessmentDistribution() {
        List<SelfAssessmentResult> results = assessmentRepository.findAll();
        long low = results.stream().filter(r -> r.getScore() <= 5).count();
        long moderate = results.stream().filter(r -> r.getScore() >= 6 && r.getScore() <= 10).count();
        long high = results.stream().filter(r -> r.getScore() >= 11 && r.getScore() <= 15).count();
        long severe = results.stream().filter(r -> r.getScore() >= 16).count();
        long total = results.size();

        Map<String, Object> distribution = new HashMap<>();
        distribution.put("lowRisk", low);
        distribution.put("moderateRisk", moderate);
        distribution.put("highRisk", high);
        distribution.put("severeRisk", severe);
        distribution.put("totalAssessments", total);
        return distribution;
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
        
        dashboardData.put("appointments", getTodayAppointmentsStats(null));
        dashboardData.put("pendingRequests", getPendingRequestsStats());
        dashboardData.put("flaggedStudents", getFlaggedStudents());
        dashboardData.put("activeStudents", getActiveStudentsStats());
        dashboardData.put("recentAssessments", getRecentAssessments());
        dashboardData.put("counselorStats", getCounselorStats());
        dashboardData.put("quickStats", getQuickStats());
        dashboardData.put("moodTrends", getMoodTrends());
        dashboardData.put("upcomingAppointments", getUpcomingAppointments(null));
        dashboardData.put("performanceMetrics", getPerformanceMetrics());
        
        return dashboardData;
    }
}

package com.example.MentalMind.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.example.MentalMind.model.Feedback;
import com.example.MentalMind.model.CounselorResponse;
import com.example.MentalMind.model.User;
import com.example.MentalMind.service.FeedbackService;
import com.example.MentalMind.service.DashboardService;
import com.example.MentalMind.repository.CounselorResponseRepository;
import com.example.MentalMind.repository.UserRepository;
import java.util.Map;

@Controller
@RequestMapping("/counselor")
public class CounselorController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private CounselorResponseRepository responseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "counselor/dashboard";
    }

    // Dashboard API endpoints for dynamic data
    @GetMapping("/api/dashboard-data")
    @ResponseBody
    public ResponseEntity<?> getDashboardData(HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || session.getAttribute("userRole") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Forbidden"));
        }
        try {
            Map<String, Object> data = dashboardService.getCompleteDashboardData();
            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Error loading dashboard data"));
        }
    }

    @GetMapping("/api/appointments-today")
    @ResponseBody
    public ResponseEntity<?> getAppointmentsToday(HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false));
        }
        try {
            Map<String, Object> stats = dashboardService.getTodayAppointmentsStats();
            return ResponseEntity.ok(Map.of("success", true, "data", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false));
        }
    }

    @GetMapping("/api/pending-requests")
    @ResponseBody
    public ResponseEntity<?> getPendingRequests(HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false));
        }
        try {
            Map<String, Object> stats = dashboardService.getPendingRequestsStats();
            return ResponseEntity.ok(Map.of("success", true, "data", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false));
        }
    }

    @GetMapping("/api/flagged-students")
    @ResponseBody
    public ResponseEntity<?> getFlaggedStudents(HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false));
        }
        try {
            java.util.List<Map<String, Object>> students = dashboardService.getFlaggedStudents();
            return ResponseEntity.ok(Map.of("success", true, "data", students));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false));
        }
    }

    @GetMapping("/api/active-students")
    @ResponseBody
    public ResponseEntity<?> getActiveStudents(HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false));
        }
        try {
            Map<String, Object> stats = dashboardService.getActiveStudentsStats();
            return ResponseEntity.ok(Map.of("success", true, "data", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false));
        }
    }

    @GetMapping("/api/recent-assessments")
    @ResponseBody
    public ResponseEntity<?> getRecentAssessments(HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false));
        }
        try {
            java.util.List<Map<String, Object>> assessments = dashboardService.getRecentAssessments();
            return ResponseEntity.ok(Map.of("success", true, "data", assessments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false));
        }
    }

    @GetMapping("/api/counselor-stats")
    @ResponseBody
    public ResponseEntity<?> getCounselorStats(HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false));
        }
        try {
            Map<String, Object> stats = dashboardService.getCounselorStats();
            return ResponseEntity.ok(Map.of("success", true, "data", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false));
        }
    }

    @GetMapping("/api/quick-stats")
    @ResponseBody
    public ResponseEntity<?> getQuickStats(HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false));
        }
        try {
            Map<String, Object> stats = dashboardService.getQuickStats();
            return ResponseEntity.ok(Map.of("success", true, "data", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false));
        }
    }

    @GetMapping("/api/mood-trends")
    @ResponseBody
    public ResponseEntity<?> getMoodTrends(HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false));
        }
        try {
            java.util.List<Map<String, Object>> trends = dashboardService.getMoodTrends();
            return ResponseEntity.ok(Map.of("success", true, "data", trends));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false));
        }
    }

    @GetMapping("/api/upcoming-appointments")
    @ResponseBody
    public ResponseEntity<?> getUpcomingAppointments(HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false));
        }
        try {
            java.util.List<Map<String, Object>> appointments = dashboardService.getUpcomingAppointments();
            return ResponseEntity.ok(Map.of("success", true, "data", appointments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false));
        }
    }

    @GetMapping("/api/performance-metrics")
    @ResponseBody
    public ResponseEntity<?> getPerformanceMetrics(HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false));
        }
        try {
            Map<String, Object> metrics = dashboardService.getPerformanceMetrics();
            return ResponseEntity.ok(Map.of("success", true, "data", metrics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false));
        }
    }

    @GetMapping("/appointments")
    public String appointments() {
        return "counselor/appointments";
    }

    @PostMapping("/appointments/approve")
    public String approveAppointment(@RequestParam String appointmentId, HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || appointmentId == null || appointmentId.isEmpty()) {
            return "redirect:/counselor/appointments?error=invalid";
        }
        session.setAttribute("lastAction", "approved");
        session.setAttribute("appointmentId", appointmentId);
        return "redirect:/counselor/appointments?success=approved";
    }

    @PostMapping("/appointments/reject")
    public String rejectAppointment(@RequestParam String appointmentId,
                                   @RequestParam(required = false) String reason,
                                   HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || appointmentId == null || appointmentId.isEmpty()) {
            return "redirect:/counselor/appointments?error=invalid";
        }
        session.setAttribute("lastAction", "rejected");
        session.setAttribute("appointmentId", appointmentId);
        session.setAttribute("rejectionReason", reason != null ? reason : "No reason provided");
        return "redirect:/counselor/appointments?success=rejected";
    }

    @GetMapping("/students")
    public String students() {
        return "counselor/students";
    }

    @GetMapping("/resources")
    public String resources() {
        return "counselor/resources";
    }

    @PostMapping("/resources/upload")
    public String uploadResource(@RequestParam String resourceTitle,
                               @RequestParam String resourceType,
                               @RequestParam String resourceUrl,
                               @RequestParam(required = false) String description,
                               HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || resourceTitle == null || resourceTitle.isEmpty() ||
            resourceUrl == null || resourceUrl.isEmpty()) {
            return "redirect:/counselor/resources?error=invalid";
        }
        session.setAttribute("lastResource", resourceTitle);
        session.setAttribute("resourceType", resourceType);
        session.setAttribute("resourceDescription", description != null ? description : "");
        return "redirect:/counselor/resources?success=uploaded";
    }

    @GetMapping("/forum")
    public String forum() {
        return "counselor/forum";
    }

    @PostMapping("/forum/moderate")
    public String moderatePost(@RequestParam String postId,
                             @RequestParam String action,
                             @RequestParam(required = false) String moderationNote,
                             HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || postId == null || postId.isEmpty() ||
            action == null || (!action.equals("approve") && !action.equals("reject") && !action.equals("flag"))) {
            return "redirect:/counselor/forum?error=invalid";
        }
        session.setAttribute("lastModerationAction", action);
        session.setAttribute("moderatedPostId", postId);
        session.setAttribute("moderationNote", moderationNote != null ? moderationNote : "");
        return "redirect:/counselor/forum?success=moderated";
    }

    @GetMapping("/reports")
    public String reports() {
        return "counselor/reports";
    }

    @GetMapping("/feedback/recent")
    @ResponseBody
    public ResponseEntity<?> recentFeedback(HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || session.getAttribute("userRole") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("success", false, "message", "Forbidden"));
        }
        try {
            java.util.List<Feedback> recent = feedbackService.getRecentFeedback(5);
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (Feedback f : recent) {
                out.add(java.util.Map.of(
                        "id", f.getId(),
                        "type", f.getType(),
                        "subject", f.getSubject(),
                        "details", f.getDetails(),
                        "status", f.getStatus(),
                        "createdAt", f.getCreatedAt().toString(),
                        "userId", f.getUser() != null ? f.getUser().getId() : null
                ));
            }
            return ResponseEntity.ok(java.util.Map.of("success", true, "feedback", out));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("success", false, "message", "Error loading feedback"));
        }
    }

    @GetMapping("/feedback/{id}")
    @ResponseBody
    public ResponseEntity<?> feedbackDetail(@org.springframework.web.bind.annotation.PathVariable Long id, HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || session.getAttribute("userRole") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("success", false, "message", "Forbidden"));
        }
        try {
            java.util.Optional<Feedback> opt = feedbackService.getFeedbackById(id);
            if (opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("success", false, "message", "Not found"));
            }
            Feedback f = opt.get();
            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "feedback", java.util.Map.of(
                            "id", f.getId(),
                            "type", f.getType(),
                            "subject", f.getSubject(),
                            "details", f.getDetails(),
                            "status", f.getStatus(),
                            "createdAt", f.getCreatedAt().toString(),
                            "userId", f.getUser() != null ? f.getUser().getId() : null
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("success", false, "message", "Error"));
        }
    }

    @PostMapping("/feedback/update-status")
    @ResponseBody
    public ResponseEntity<?> updateFeedbackStatus(@RequestParam Long id, @RequestParam String status, HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || session.getAttribute("userRole") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("success", false, "message", "Forbidden"));
        }
        if (status == null || !(status.equals("pending") || status.equals("reviewed") || status.equals("resolved"))) {
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", "Invalid status"));
        }
        try {
            Feedback updated = feedbackService.updateFeedbackStatus(id, status);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("success", false, "message", "Not found"));
            }
            return ResponseEntity.ok(java.util.Map.of("success", true, "feedback", java.util.Map.of("id", updated.getId(), "status", updated.getStatus())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("success", false, "message", "Error updating status"));
        }
    }

    @GetMapping("/settings")
    public String settings() {
        return "counselor/settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam(required = false) String fullName,
                               @RequestParam(required = false) String specialization,
                               @RequestParam(required = false) String availabilityHours,
                               @RequestParam(required = false) String bio,
                               @RequestParam(defaultValue = "false") String notificationsEnabled,
                               HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null) {
            return "redirect:/counselor/settings?error=unauthorized";
        }
        session.setAttribute("counselorName", fullName != null ? fullName : "");
        session.setAttribute("specialization", specialization != null ? specialization : "");
        session.setAttribute("availabilityHours", availabilityHours != null ? availabilityHours : "");
        session.setAttribute("bio", bio != null ? bio : "");
        session.setAttribute("notificationsEnabled", notificationsEnabled);
        return "redirect:/counselor/settings?success=updated";
    }

    @GetMapping("/feedback-form")
    public String feedbackForm() {
        return "counselor/feedback-form";
    }

    @PostMapping("/feedback/respond")
    @ResponseBody
    public ResponseEntity<?> respondToFeedback(@RequestParam Long feedbackId, 
                                               @RequestParam String responseType, 
                                               @RequestParam String message, 
                                               HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || session.getAttribute("userRole") == null || !"counselor".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("success", false, "message", "Forbidden"));
        }

        if (feedbackId == null || message == null || message.isEmpty() || responseType == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", "Invalid input"));
        }

        if (!responseType.matches("acknowledgement|action|followup")) {
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", "Invalid response type"));
        }

        try {
            java.util.Optional<Feedback> feedback = feedbackService.getFeedbackById(feedbackId);
            if (feedback.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("success", false, "message", "Feedback not found"));
            }

            Long counselorId = (Long) session.getAttribute("userId");
            java.util.Optional<User> counselor = userRepository.findById(counselorId);
            if (counselor.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("success", false, "message", "Counselor not found"));
            }

            CounselorResponse response = new CounselorResponse(feedback.get(), counselor.get(), responseType, message);
            CounselorResponse saved = responseRepository.save(response);

            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "message", "Response recorded successfully",
                    "responseId", saved.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("success", false, "message", "Error saving response"));
        }
    }
}

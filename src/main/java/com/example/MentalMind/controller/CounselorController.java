package com.example.MentalMind.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/counselor")
public class CounselorController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "counselor/dashboard";
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
}

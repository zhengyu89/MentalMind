package com.example.MentalMind.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/student")
public class StudentController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "student/dashboard";
    }

    @GetMapping("/learning")
    public String learning() {
        return "student/learning";
    }

    @GetMapping("/mood-tracker")
    public String moodTracker() {
        return "student/mood-tracker";
    }

    @GetMapping("/resources")
    public String resources() {
        return "student/resources";
    }

    @GetMapping("/forum")
    public String forum() {
        return "student/forum";
    }

    @PostMapping("/forum/post")
    public String createForumPost(@RequestParam String title,
                                @RequestParam String content,
                                @RequestParam(required = false) String category,
                                @RequestParam(defaultValue = "false") String anonymous,
                                HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || title == null || title.isEmpty() ||
            content == null || content.isEmpty()) {
            return "redirect:/student/forum?error=invalid";
        }
        session.setAttribute("lastPost", title);
        session.setAttribute("postCategory", category != null ? category : "General");
        session.setAttribute("postAnonymous", anonymous);
        return "redirect:/student/forum?success=posted";
    }

    @GetMapping("/appointments")
    public String appointments() {
        return "student/appointments";
    }

    @PostMapping("/appointments/request")
    public String requestAppointment(@RequestParam String counselorId,
                                    @RequestParam String preferredDate,
                                    @RequestParam String preferredTime,
                                    @RequestParam(required = false) String reason,
                                    HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || counselorId == null || counselorId.isEmpty() ||
            preferredDate == null || preferredDate.isEmpty() || preferredTime == null || preferredTime.isEmpty()) {
            return "redirect:/student/appointments?error=invalid";
        }
        session.setAttribute("lastAppointmentRequest", counselorId);
        session.setAttribute("preferredDate", preferredDate);
        session.setAttribute("preferredTime", preferredTime);
        session.setAttribute("appointmentReason", reason != null ? reason : "");
        return "redirect:/student/appointments?success=requested";
    }

    @GetMapping("/emergency")
    public String emergency() {
        return "student/emergency";
    }

    @GetMapping("/recommendations")
    public String recommendations() {
        return "student/recommendations";
    }

    @GetMapping("/feedback")
    public String feedback() {
        return "student/feedback";
    }

    @PostMapping("/feedback/submit")
    public String submitFeedback(@RequestParam String feedbackType,
                               @RequestParam String message,
                               @RequestParam(required = false) String rating,
                               @RequestParam(required = false) String email,
                               HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || message == null || message.isEmpty() ||
            feedbackType == null || feedbackType.isEmpty()) {
            return "redirect:/student/feedback?error=invalid";
        }
        session.setAttribute("lastFeedbackType", feedbackType);
        session.setAttribute("feedbackRating", rating != null ? rating : "");
        session.setAttribute("feedbackContact", email != null ? email : "");
        return "redirect:/student/feedback?success=submitted";
    }
}

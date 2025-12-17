package com.example.MentalMind.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String createForumPost() {
        // TODO: Handle forum post creation
        return "redirect:/student/forum";
    }

    @GetMapping("/appointments")
    public String appointments() {
        return "student/appointments";
    }

    @PostMapping("/appointments/request")
    public String requestAppointment() {
        // TODO: Handle appointment request
        return "redirect:/student/appointments";
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
    public String submitFeedback() {
        // TODO: Handle feedback submission
        return "redirect:/student/feedback";
    }
}

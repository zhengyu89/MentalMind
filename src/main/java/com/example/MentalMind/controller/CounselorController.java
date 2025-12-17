package com.example.MentalMind.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String approveAppointment() {
        // TODO: Handle appointment approval
        return "redirect:/counselor/appointments";
    }

    @PostMapping("/appointments/reject")
    public String rejectAppointment() {
        // TODO: Handle appointment rejection
        return "redirect:/counselor/appointments";
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
    public String uploadResource() {
        // TODO: Handle resource upload
        return "redirect:/counselor/resources";
    }

    @GetMapping("/forum")
    public String forum() {
        return "counselor/forum";
    }

    @PostMapping("/forum/moderate")
    public String moderatePost() {
        // TODO: Handle post moderation
        return "redirect:/counselor/forum";
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
    public String updateSettings() {
        // TODO: Handle settings update
        return "redirect:/counselor/settings";
    }
}

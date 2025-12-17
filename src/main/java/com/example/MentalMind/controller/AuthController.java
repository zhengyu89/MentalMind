package com.example.MentalMind.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "student") String role) {
        // TODO: Add actual authentication logic here
        // For now, redirect based on role selection
        if ("counselor".equals(role)) {
            return "redirect:/counselor/dashboard";
        }
        return "redirect:/student/dashboard";
    }

    @GetMapping("/logout")
    public String logout() {
        // TODO: Add actual logout logic (clear session, etc.)
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        // TODO: Create register page
        return "redirect:/login";
    }
}

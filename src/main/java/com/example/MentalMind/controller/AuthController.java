package com.example.MentalMind.controller;

import com.example.MentalMind.model.User;
import com.example.MentalMind.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
    
    @Autowired
    private AuthenticationService authenticationService;

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
            @RequestParam(defaultValue = "student") String role,
            HttpSession session) {
        
        User user = authenticationService.authenticate(email, password, role);
        
        if (user != null) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("userFullName", user.getFullName());
            session.setAttribute("isAuthenticated", true);
            
            if ("counselor".equals(user.getRole())) {
                return "redirect:/counselor/dashboard";
            }
            return "redirect:/student/dashboard";
        }
        
        return "redirect:/login?error=invalid";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=success";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }
    
    @PostMapping("/register")
    public String processRegister(@RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(defaultValue = "student") String role,
            HttpSession session) {
        
        User newUser = authenticationService.register(email, password, confirmPassword, role);
        
        if (newUser != null) {
            session.setAttribute("userId", newUser.getId());
            session.setAttribute("userEmail", newUser.getEmail());
            session.setAttribute("userRole", newUser.getRole());
            session.setAttribute("isAuthenticated", true);
            
            if ("counselor".equals(newUser.getRole())) {
                return "redirect:/counselor/dashboard";
            }
            return "redirect:/student/dashboard";
        }
        
        return "redirect:/register?error=invalid";
    }
}

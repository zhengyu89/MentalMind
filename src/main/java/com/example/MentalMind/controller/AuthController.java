package com.example.MentalMind.controller;

import com.example.MentalMind.model.User;
import com.example.MentalMind.service.AuthenticationService;
import com.example.MentalMind.exception.UserNotFoundException;
import com.example.MentalMind.exception.InvalidPasswordException;
import com.example.MentalMind.exception.PasswordMismatchException;
import com.example.MentalMind.exception.EmailAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        logger.debug("Showing login page");
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "student") String role,
            HttpSession session,
            Model model) {
        
        logger.debug("Login attempt - Email: {}, Role: {}", email, role);
        
        try {
            // Authenticate using our service
            User user = authenticationService.authenticate(email, password, role);
            logger.debug("Authentication successful for user: {} with role: {}", email, role);
            
            // Store user info in session - this is all we need with custom interceptor
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("userFullName", user.getFullName());
            session.setAttribute("isAuthenticated", true);
            
            logger.debug("Session attributes set. User: {}, Role: {}", email, user.getRole());
            logger.debug("Redirecting to {}...", user.getRole().equals("counselor") ? "counselor/dashboard" : "student/dashboard");
            
            if ("counselor".equals(user.getRole())) {
                return "redirect:/counselor/dashboard";
            }
            return "redirect:/student/dashboard";
        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", email);
            model.addAttribute("error", "Email not registered. Please check your email or register a new account.");
            return "login";
        } catch (InvalidPasswordException e) {
            logger.error("Invalid password for user: {}", email);
            model.addAttribute("error", "Incorrect password. Please try again.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Invalidate session
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
            HttpSession session,
            Model model) {
        
        try {
            User newUser = authenticationService.register(email, password, confirmPassword, role);
            
            // Store user info in session
            session.setAttribute("userId", newUser.getId());
            session.setAttribute("userEmail", newUser.getEmail());
            session.setAttribute("userRole", newUser.getRole());
            session.setAttribute("userFullName", newUser.getFullName());
            session.setAttribute("isAuthenticated", true);
            
            logger.debug("Registration successful for user: {} with role: {}", email, newUser.getRole());
            
            if ("counselor".equals(newUser.getRole())) {
                return "redirect:/counselor/dashboard";
            }
            return "redirect:/student/dashboard";
        } catch (PasswordMismatchException e) {
            model.addAttribute("error", "Passwords do not match. Please try again.");
            return "register";
        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("error", "Email already registered. Please use a different email or try logging in.");
            return "register";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Email and password are required.");
            return "register";
        }
    }
}

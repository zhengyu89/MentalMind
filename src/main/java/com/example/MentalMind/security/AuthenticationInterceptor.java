package com.example.MentalMind.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("Request: {} {}", method, requestPath);
        
        // Allow public paths for both GET and POST
        if (requestPath.equals("/") || requestPath.equals("/login") || requestPath.equals("/register") ||
                requestPath.startsWith("/css/") || requestPath.startsWith("/js/") || 
                requestPath.startsWith("/assets/") || requestPath.equals("/error")) {
            logger.debug("Public path allowed: {}", requestPath);
            return true;
        }
        
        // Check if user is authenticated
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("isAuthenticated") == null) {
            logger.debug("User not authenticated, redirecting to login");
            response.sendRedirect("/login?redirect=" + requestPath);
            return false;
        }
        
        // Check role-based access
        String userRole = (String) session.getAttribute("userRole");
        
        // Handle null or empty role - redirect to login
        if (userRole == null || userRole.isEmpty()) {
            logger.warn("User role is null or empty, redirecting to login");
            session.invalidate();
            response.sendRedirect("/login?error=session_expired");
            return false;
        }
        
        if (requestPath.startsWith("/student/") && !"student".equalsIgnoreCase(userRole)) {
            logger.warn("Unauthorized access to student path with role: {}", userRole);
            // Redirect to appropriate dashboard instead of 403
            if ("counselor".equalsIgnoreCase(userRole)) {
                response.sendRedirect("/counselor/dashboard");
            } else {
                response.sendRedirect("/login?error=unauthorized");
            }
            return false;
        }
        
        if (requestPath.startsWith("/counselor/") && !"counselor".equalsIgnoreCase(userRole)) {
            logger.warn("Unauthorized access to counselor path with role: {}", userRole);
            // Redirect to appropriate dashboard instead of 403
            if ("student".equalsIgnoreCase(userRole)) {
                response.sendRedirect("/student/dashboard");
            } else {
                response.sendRedirect("/login?error=unauthorized");
            }
            return false;
        }
        
        logger.debug("Authorization passed for path: {}", requestPath);
        return true;
    }
}

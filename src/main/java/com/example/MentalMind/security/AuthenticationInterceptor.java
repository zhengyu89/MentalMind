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
                requestPath.startsWith("/assets/")) {
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
        
        if (requestPath.startsWith("/student/") && !"student".equalsIgnoreCase(userRole)) {
            logger.warn("Unauthorized access to student path with role: {}", userRole);
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        
        if (requestPath.startsWith("/counselor/") && !"counselor".equalsIgnoreCase(userRole)) {
            logger.warn("Unauthorized access to counselor path with role: {}", userRole);
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        
        logger.debug("Authorization passed for path: {}", requestPath);
        return true;
    }
}

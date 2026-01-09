package com.example.MentalMind.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * Configure password encoder using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Get the AuthenticationManager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configure SecurityContextRepository to persist authentication in session
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
            new RequestAttributeSecurityContextRepository(),
            new HttpSessionSecurityContextRepository()
        );
    }

    /**
     * Configure HTTP security - URLs that require authentication and authorization
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable authorization checks - we use custom AuthenticationInterceptor instead
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                )
                
                // Configure security context repository for session persistence
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository())
                )
                
                // Disable Spring Security form login - we use custom AuthController
                .formLogin(form -> form.disable())
                
                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=success")
                        .permitAll()
                )
                
                // Exception handling
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/403")
                )
                
                // Disable CSRF for now (enable in production with proper token handling)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}


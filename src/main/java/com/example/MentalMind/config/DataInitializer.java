package com.example.MentalMind.config;

import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Only insert if users don't exist
        if (userRepository.findByEmail("student@example.com").isEmpty()) {
            User student = new User("student@example.com", "password123", "student");
            student.setFullName("John Student");
            userRepository.save(student);
        }
        
        if (userRepository.findByEmail("counselor@example.com").isEmpty()) {
            User counselor = new User("counselor@example.com", "password123", "counselor");
            counselor.setFullName("Jane Counselor");
            userRepository.save(counselor);
        }
    }
}

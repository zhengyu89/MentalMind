package com.example.MentalMind.service;

import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthenticationService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User authenticate(String email, String password, String role) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        
        Optional<User> user = userRepository.findByEmailAndRole(email, role);
        
        if (user.isPresent()) {
            User foundUser = user.get();
            if (foundUser.getPassword().equals(password) && foundUser.getIsActive()) {
                return foundUser;
            }
        }
        return null;
    }
    
    public User register(String email, String password, String confirmPassword, String role) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        
        if (!password.equals(confirmPassword)) {
            return null;
        }
        
        if (userRepository.existsByEmail(email)) {
            return null;
        }
        
        User newUser = new User(email, password, role);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(newUser);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public User updateUser(Long userId, String fullName, String phoneNumber) {
        Optional<User> user = userRepository.findById(userId);
        
        if (user.isPresent()) {
            User foundUser = user.get();
            if (fullName != null && !fullName.isEmpty()) {
                foundUser.setFullName(fullName);
            }
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                foundUser.setPhoneNumber(phoneNumber);
            }
            foundUser.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(foundUser);
        }
        return null;
    }
    
    public boolean deactivateUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        
        if (user.isPresent()) {
            User foundUser = user.get();
            foundUser.setIsActive(false);
            foundUser.setUpdatedAt(LocalDateTime.now());
            userRepository.save(foundUser);
            return true;
        }
        return false;
    }
}

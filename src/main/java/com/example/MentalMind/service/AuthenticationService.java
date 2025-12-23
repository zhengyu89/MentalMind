package com.example.MentalMind.service;

import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.UserRepository;
import com.example.MentalMind.exception.UserNotFoundException;
import com.example.MentalMind.exception.InvalidPasswordException;
import com.example.MentalMind.exception.PasswordMismatchException;
import com.example.MentalMind.exception.EmailAlreadyExistsException;
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
            throw new UserNotFoundException("Email and password are required");
        }
        
        Optional<User> user = userRepository.findByEmailAndRole(email, role);
        
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        
        User foundUser = user.get();
        if (!foundUser.getPassword().equals(password)) {
            throw new InvalidPasswordException("Invalid password for user: " + email);
        }
        
        if (!foundUser.getIsActive()) {
            throw new UserNotFoundException("User account is inactive");
        }
        
        return foundUser;
    }
    
    public User register(String email, String password, String confirmPassword, String role) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Email and password are required");
        }
        
        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException("Passwords do not match");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered");
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

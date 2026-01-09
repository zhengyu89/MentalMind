package com.example.MentalMind.security;

import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(email);
        
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        
        User foundUser = user.get();
        
        if (!foundUser.getIsActive()) {
            throw new UsernameNotFoundException("User account is inactive");
        }
        
        // Build UserDetails with role as authority
        return org.springframework.security.core.userdetails.User.builder()
                .username(foundUser.getEmail())
                .password(foundUser.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_" + foundUser.getRole().toUpperCase()))
                .accountLocked(false)
                .accountExpired(false)
                .credentialsExpired(false)
                .disabled(!foundUser.getIsActive())
                .build();
    }
}

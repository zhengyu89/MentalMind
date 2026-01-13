package com.example.MentalMind.repository;

import com.example.MentalMind.model.ForumPostFlag;
import com.example.MentalMind.model.ForumPost;
import com.example.MentalMind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostFlagRepository extends JpaRepository<ForumPostFlag, Long> {
    
    // Find all flags for a specific post
    List<ForumPostFlag> findByPostOrderByCreatedAtDesc(ForumPost post);
    
    // Count flags for a specific post
    long countByPost(ForumPost post);
    
    // Check if a user already flagged a post
    boolean existsByPostAndUser(ForumPost post, User user);
    
    // Delete all flags for a post
    void deleteByPost(ForumPost post);
}

package com.example.MentalMind.repository;

import com.example.MentalMind.model.ForumComment;
import com.example.MentalMind.model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {

    // Find all comments for a specific post ordered by creation date
    List<ForumComment> findByPostOrderByCreatedAtAsc(ForumPost post);

    // Count comments by post
    long countByPost(ForumPost post);
    
    // Delete all comments for a specific post
    void deleteByPost(ForumPost post);
}

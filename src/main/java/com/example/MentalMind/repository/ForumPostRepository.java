package com.example.MentalMind.repository;

import com.example.MentalMind.model.ForumPost;
import com.example.MentalMind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    // Find all approved posts ordered by creation date
    List<ForumPost> findByStatusOrderByCreatedAtDesc(String status);

    // Find approved posts by category
    @Query("SELECT p FROM ForumPost p WHERE p.status = 'APPROVED' AND p.category = :category ORDER BY p.createdAt DESC")
    List<ForumPost> findByCategoryAndApproved(@Param("category") String category);

    // Find pending posts for moderation
    List<ForumPost> findByStatusInOrderByCreatedAtDesc(List<String> statuses);

    // Find posts by user
    List<ForumPost> findByUserOrderByCreatedAtDesc(User user);

    // Find posts by user and status
    List<ForumPost> findByUserAndStatus(User user, String status);

    // Count pending posts
    long countByStatus(String status);
}

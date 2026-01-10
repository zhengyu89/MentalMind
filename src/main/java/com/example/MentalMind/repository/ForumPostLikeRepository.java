package com.example.MentalMind.repository;

import com.example.MentalMind.model.ForumPost;
import com.example.MentalMind.model.ForumPostLike;
import com.example.MentalMind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ForumPostLikeRepository extends JpaRepository<ForumPostLike, Long> {
    boolean existsByPostAndUser(ForumPost post, User user);
    Optional<ForumPostLike> findByPostAndUser(ForumPost post, User user);
    long countByPost(ForumPost post);
    void deleteByPostAndUser(ForumPost post, User user);
    void deleteByPost(ForumPost post);
    List<ForumPostLike> findByUser(User user);
}

package com.example.MentalMind.repository;

import com.example.MentalMind.model.UserResourceBookmark;
import com.example.MentalMind.model.User;
import com.example.MentalMind.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserResourceBookmarkRepository extends JpaRepository<UserResourceBookmark, Long> {
    List<UserResourceBookmark> findByUserAndIsActiveTrue(User user);
    Optional<UserResourceBookmark> findByUserAndResourceAndIsActiveTrue(User user, Resource resource);
    boolean existsByUserAndResourceAndBookmarkTypeAndIsActiveTrue(User user, Resource resource, String bookmarkType);
}
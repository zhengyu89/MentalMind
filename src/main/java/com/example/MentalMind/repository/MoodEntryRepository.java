package com.example.MentalMind.repository;

import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoodEntryRepository extends JpaRepository<MoodEntry, Long> {

    // Find all mood entries for a user ordered by date descending
    List<MoodEntry> findByUserOrderByCreatedAtDesc(User user);

    // Find mood entries for a user within a date range
    List<MoodEntry> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            User user, LocalDateTime start, LocalDateTime end);

    // Find today's mood entry for a user
    @Query("SELECT m FROM MoodEntry m WHERE m.user = :user AND DATE(m.createdAt) = CURRENT_DATE")
    Optional<MoodEntry> findTodaysMoodByUser(@Param("user") User user);

    // Get the last N mood entries for a user
    List<MoodEntry> findTop7ByUserOrderByCreatedAtDesc(User user);

    // Calculate average mood for a user
    @Query("SELECT AVG(m.moodScore) FROM MoodEntry m WHERE m.user = :user")
    Double getAverageMoodScore(@Param("user") User user);

    // Count consecutive days with mood entries (for streak calculation)
    @Query("SELECT COUNT(DISTINCT DATE(m.createdAt)) FROM MoodEntry m WHERE m.user = :user AND m.createdAt >= :startDate")
    Long countDistinctDaysSince(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    // Get the most common mood score for a user
    @Query("SELECT m.moodScore FROM MoodEntry m WHERE m.user = :user GROUP BY m.moodScore ORDER BY COUNT(m.moodScore) DESC LIMIT 1")
    Optional<Integer> findMostCommonMoodScore(@Param("user") User user);
}

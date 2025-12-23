package com.example.MentalMind.service;

import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.MoodEntryRepository;
import com.example.MentalMind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MoodService {

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Log a mood entry for a user
     */
    @Transactional
    public MoodEntry logMood(Long userId, Integer moodScore, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if mood already logged today
        Optional<MoodEntry> existingEntry = moodEntryRepository.findTodaysMoodByUser(user);

        if (existingEntry.isPresent()) {
            // Update existing entry
            MoodEntry entry = existingEntry.get();
            entry.setMoodScore(moodScore);
            entry.setNotes(notes);
            return moodEntryRepository.save(entry);
        } else {
            // Create new entry
            MoodEntry entry = new MoodEntry(user, moodScore, notes);
            return moodEntryRepository.save(entry);
        }
    }

    /**
     * Get today's mood for a user
     */
    public Optional<MoodEntry> getTodaysMood(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return moodEntryRepository.findTodaysMoodByUser(user);
    }

    /**
     * Get the last 7 mood entries for a user (for weekly chart)
     */
    public List<MoodEntry> getWeeklyMoods(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return moodEntryRepository.findTop7ByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Get mood entries within a date range
     */
    public List<MoodEntry> getMoodsInRange(Long userId, LocalDateTime start, LocalDateTime end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return moodEntryRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, start, end);
    }

    /**
     * Get all mood entries for a user
     */
    public List<MoodEntry> getAllMoods(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return moodEntryRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Calculate mood statistics for a user
     */
    public Map<String, Object> getMoodStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> stats = new HashMap<>();

        // Average mood score
        Double avgMood = moodEntryRepository.getAverageMoodScore(user);
        stats.put("averageMood", avgMood != null ? Math.round(avgMood * 10.0) / 10.0 : 0.0);

        // Most common mood
        Optional<Integer> mostCommon = moodEntryRepository.findMostCommonMoodScore(user);
        stats.put("mostCommonMood", mostCommon.orElse(3));
        stats.put("mostCommonEmoji", getMoodEmoji(mostCommon.orElse(3)));

        // Streak calculation
        stats.put("streak", calculateStreak(user));

        return stats;
    }

    /**
     * Get monthly moods as a map of date string (yyyy-MM-dd) to MoodEntry
     */
    public Map<String, MoodEntry> getMonthlyMoodsMap(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();

        List<MoodEntry> entries = moodEntryRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
                user, startOfMonth, endOfMonth);

        Map<String, MoodEntry> moodMap = new HashMap<>();
        for (MoodEntry entry : entries) {
            String dateKey = entry.getCreatedAt().toLocalDate().toString();
            moodMap.put(dateKey, entry);
        }
        return moodMap;
    }

    /**
     * Get weekly moods as a map of date string (yyyy-MM-dd) to MoodEntry
     */
    public Map<String, MoodEntry> getWeeklyMoodsMap(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime startOfWeek = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime endOfWeek = LocalDate.now().plusDays(1).atStartOfDay();

        List<MoodEntry> entries = moodEntryRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
                user, startOfWeek, endOfWeek);

        Map<String, MoodEntry> moodMap = new HashMap<>();
        for (MoodEntry entry : entries) {
            String dateKey = entry.getCreatedAt().toLocalDate().toString();
            moodMap.put(dateKey, entry);
        }
        return moodMap;
    }

    /**
     * Calculate the current mood logging streak
     */
    private int calculateStreak(User user) {
        List<MoodEntry> entries = moodEntryRepository.findByUserOrderByCreatedAtDesc(user);
        if (entries.isEmpty()) {
            return 0;
        }

        int streak = 0;
        LocalDate expectedDate = LocalDate.now();

        // Check if today has an entry
        boolean hasTodayEntry = entries.stream()
                .anyMatch(e -> e.getCreatedAt().toLocalDate().equals(LocalDate.now()));

        if (!hasTodayEntry) {
            expectedDate = expectedDate.minusDays(1);
        }

        Set<LocalDate> entryDates = new HashSet<>();
        for (MoodEntry entry : entries) {
            entryDates.add(entry.getCreatedAt().toLocalDate());
        }

        while (entryDates.contains(expectedDate)) {
            streak++;
            expectedDate = expectedDate.minusDays(1);
        }

        return streak;
    }

    /**
     * Get emoji for a mood score
     */
    private String getMoodEmoji(int score) {
        return switch (score) {
            case 1 -> "😔";
            case 2 -> "😟";
            case 3 -> "😐";
            case 4 -> "🙂";
            case 5 -> "😄";
            default -> "❓";
        };
    }
}

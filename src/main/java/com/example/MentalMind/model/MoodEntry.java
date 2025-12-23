package com.example.MentalMind.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mood_entries")
public class MoodEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer moodScore;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public MoodEntry() {
        this.createdAt = LocalDateTime.now();
    }

    public MoodEntry(User user, Integer moodScore, String notes) {
        this.user = user;
        this.moodScore = moodScore;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(Integer moodScore) {
        this.moodScore = moodScore;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper method to get mood emoji
    public String getMoodEmoji() {
        return switch (moodScore) {
            case 1 -> "😔";
            case 2 -> "😟";
            case 3 -> "😐";
            case 4 -> "🙂";
            case 5 -> "😄";
            default -> "❓";
        };
    }

    // Helper method to get mood label
    public String getMoodLabel() {
        return switch (moodScore) {
            case 1 -> "Terrible";
            case 2 -> "Bad";
            case 3 -> "Okay";
            case 4 -> "Good";
            case 5 -> "Great";
            default -> "Unknown";
        };
    }
}

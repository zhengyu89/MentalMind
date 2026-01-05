package com.example.MentalMind.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "counselor_responses")
public class CounselorResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private User counselor;

    @Column(nullable = false)
    private String responseType; // "acknowledgement", "action", "followup"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public CounselorResponse() {
        this.createdAt = LocalDateTime.now();
    }

    public CounselorResponse(Feedback feedback, User counselor, String responseType, String message) {
        this.feedback = feedback;
        this.counselor = counselor;
        this.responseType = responseType;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public User getCounselor() {
        return counselor;
    }

    public void setCounselor(User counselor) {
        this.counselor = counselor;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

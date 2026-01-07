package com.example.MentalMind.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private User counselor;

    @Column(nullable = false)
    private LocalDateTime appointmentDateTime;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, COMPLETED, CANCELLED, REJECTED

    @Column(nullable = true, columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    public Appointment() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Appointment(User student, User counselor, LocalDateTime appointmentDateTime, String reason) {
        this.student = student;
        this.counselor = counselor;
        this.appointmentDateTime = appointmentDateTime;
        this.reason = reason;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public User getCounselor() {
        return counselor;
    }

    public void setCounselor(User counselor) {
        this.counselor = counselor;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper method to check if appointment is in the future
    public boolean isUpcoming() {
        return appointmentDateTime.isAfter(LocalDateTime.now());
    }

    // Helper method to check if appointment is in the past
    public boolean isPast() {
        return appointmentDateTime.isBefore(LocalDateTime.now());
    }

    // Helper method to format date for display
    public String getFormattedDate() {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return appointmentDateTime.format(formatter);
    }

    // Helper method to format time for display
    public String getFormattedTime() {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a");
        return appointmentDateTime.format(formatter);
    }
}

package com.example.MentalMind.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "counselor_settings")
public class CounselorSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "counselor_id", nullable = false, unique = true)
    private User counselor;

    // Profile fields
    @Column(length = 1000)
    private String bio;

    @Column(length = 255)
    private String specialization;

    @Column(length = 500)
    private String profilePhotoUrl;

    // Availability fields - stored as simple time strings
    @Column(length = 10)
    private String mondayStart = "09:00";
    @Column(length = 10)
    private String mondayEnd = "17:00";
    private Boolean mondayActive = true;

    @Column(length = 10)
    private String tuesdayStart = "09:00";
    @Column(length = 10)
    private String tuesdayEnd = "17:00";
    private Boolean tuesdayActive = true;

    @Column(length = 10)
    private String wednesdayStart = "09:00";
    @Column(length = 10)
    private String wednesdayEnd = "17:00";
    private Boolean wednesdayActive = true;

    @Column(length = 10)
    private String thursdayStart = "09:00";
    @Column(length = 10)
    private String thursdayEnd = "17:00";
    private Boolean thursdayActive = true;

    @Column(length = 10)
    private String fridayStart = "09:00";
    @Column(length = 10)
    private String fridayEnd = "15:00";
    private Boolean fridayActive = true;

    // Notification preferences
    private Boolean notifyAppointments = true;
    private Boolean notifyHighRisk = true;
    private Boolean notifyForumReports = true;

    @Column(nullable = true)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    public CounselorSettings() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public CounselorSettings(User counselor) {
        this();
        this.counselor = counselor;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCounselor() {
        return counselor;
    }

    public void setCounselor(User counselor) {
        this.counselor = counselor;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    // Monday
    public String getMondayStart() {
        return mondayStart;
    }

    public void setMondayStart(String mondayStart) {
        this.mondayStart = mondayStart;
    }

    public String getMondayEnd() {
        return mondayEnd;
    }

    public void setMondayEnd(String mondayEnd) {
        this.mondayEnd = mondayEnd;
    }

    public Boolean getMondayActive() {
        return mondayActive;
    }

    public void setMondayActive(Boolean mondayActive) {
        this.mondayActive = mondayActive;
    }

    // Tuesday
    public String getTuesdayStart() {
        return tuesdayStart;
    }

    public void setTuesdayStart(String tuesdayStart) {
        this.tuesdayStart = tuesdayStart;
    }

    public String getTuesdayEnd() {
        return tuesdayEnd;
    }

    public void setTuesdayEnd(String tuesdayEnd) {
        this.tuesdayEnd = tuesdayEnd;
    }

    public Boolean getTuesdayActive() {
        return tuesdayActive;
    }

    public void setTuesdayActive(Boolean tuesdayActive) {
        this.tuesdayActive = tuesdayActive;
    }

    // Wednesday
    public String getWednesdayStart() {
        return wednesdayStart;
    }

    public void setWednesdayStart(String wednesdayStart) {
        this.wednesdayStart = wednesdayStart;
    }

    public String getWednesdayEnd() {
        return wednesdayEnd;
    }

    public void setWednesdayEnd(String wednesdayEnd) {
        this.wednesdayEnd = wednesdayEnd;
    }

    public Boolean getWednesdayActive() {
        return wednesdayActive;
    }

    public void setWednesdayActive(Boolean wednesdayActive) {
        this.wednesdayActive = wednesdayActive;
    }

    // Thursday
    public String getThursdayStart() {
        return thursdayStart;
    }

    public void setThursdayStart(String thursdayStart) {
        this.thursdayStart = thursdayStart;
    }

    public String getThursdayEnd() {
        return thursdayEnd;
    }

    public void setThursdayEnd(String thursdayEnd) {
        this.thursdayEnd = thursdayEnd;
    }

    public Boolean getThursdayActive() {
        return thursdayActive;
    }

    public void setThursdayActive(Boolean thursdayActive) {
        this.thursdayActive = thursdayActive;
    }

    // Friday
    public String getFridayStart() {
        return fridayStart;
    }

    public void setFridayStart(String fridayStart) {
        this.fridayStart = fridayStart;
    }

    public String getFridayEnd() {
        return fridayEnd;
    }

    public void setFridayEnd(String fridayEnd) {
        this.fridayEnd = fridayEnd;
    }

    public Boolean getFridayActive() {
        return fridayActive;
    }

    public void setFridayActive(Boolean fridayActive) {
        this.fridayActive = fridayActive;
    }

    // Notifications
    public Boolean getNotifyAppointments() {
        return notifyAppointments;
    }

    public void setNotifyAppointments(Boolean notifyAppointments) {
        this.notifyAppointments = notifyAppointments;
    }

    public Boolean getNotifyHighRisk() {
        return notifyHighRisk;
    }

    public void setNotifyHighRisk(Boolean notifyHighRisk) {
        this.notifyHighRisk = notifyHighRisk;
    }

    public Boolean getNotifyForumReports() {
        return notifyForumReports;
    }

    public void setNotifyForumReports(Boolean notifyForumReports) {
        this.notifyForumReports = notifyForumReports;
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

    // Helper methods to format availability for display
    public String getFormattedAvailability(String day) {
        switch (day.toLowerCase()) {
            case "monday":
                return formatTime(mondayStart) + " - " + formatTime(mondayEnd);
            case "tuesday":
                return formatTime(tuesdayStart) + " - " + formatTime(tuesdayEnd);
            case "wednesday":
                return formatTime(wednesdayStart) + " - " + formatTime(wednesdayEnd);
            case "thursday":
                return formatTime(thursdayStart) + " - " + formatTime(thursdayEnd);
            case "friday":
                return formatTime(fridayStart) + " - " + formatTime(fridayEnd);
            default:
                return "";
        }
    }

    private String formatTime(String time24) {
        if (time24 == null || time24.isEmpty())
            return "";
        try {
            String[] parts = time24.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            String ampm = hour >= 12 ? "PM" : "AM";
            hour = hour % 12;
            if (hour == 0)
                hour = 12;
            return String.format("%d:%02d %s", hour, minute, ampm);
        } catch (Exception e) {
            return time24;
        }
    }
}

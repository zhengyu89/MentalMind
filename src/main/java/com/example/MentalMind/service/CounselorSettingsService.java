package com.example.MentalMind.service;

import com.example.MentalMind.model.CounselorSettings;
import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.CounselorSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CounselorSettingsService {

    @Autowired
    private CounselorSettingsRepository settingsRepository;

    /**
     * Get existing settings for a counselor, or create new default settings if none
     * exist.
     */
    public CounselorSettings getOrCreateSettings(User counselor) {
        Optional<CounselorSettings> existing = settingsRepository.findByCounselor(counselor);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new settings with defaults
        CounselorSettings settings = new CounselorSettings(counselor);
        settings.setBio("Licensed counselor helping students navigate mental health challenges.");
        settings.setSpecialization("Anxiety, Depression, Academic Stress");
        return settingsRepository.save(settings);
    }

    /**
     * Update profile information (name/email are on User entity; bio and
     * specialization are on settings)
     */
    public CounselorSettings updateProfile(User counselor, String bio, String specialization, String profilePhotoUrl) {
        CounselorSettings settings = getOrCreateSettings(counselor);

        if (bio != null) {
            settings.setBio(bio);
        }
        if (specialization != null) {
            settings.setSpecialization(specialization);
        }
        if (profilePhotoUrl != null) {
            settings.setProfilePhotoUrl(profilePhotoUrl);
        }

        settings.setUpdatedAt(LocalDateTime.now());
        return settingsRepository.save(settings);
    }

    /**
     * Update availability for a specific day
     */
    public CounselorSettings updateAvailability(User counselor, String day, String startTime, String endTime,
            Boolean isActive) {
        CounselorSettings settings = getOrCreateSettings(counselor);

        switch (day.toLowerCase()) {
            case "monday":
                if (startTime != null)
                    settings.setMondayStart(startTime);
                if (endTime != null)
                    settings.setMondayEnd(endTime);
                if (isActive != null)
                    settings.setMondayActive(isActive);
                break;
            case "tuesday":
                if (startTime != null)
                    settings.setTuesdayStart(startTime);
                if (endTime != null)
                    settings.setTuesdayEnd(endTime);
                if (isActive != null)
                    settings.setTuesdayActive(isActive);
                break;
            case "wednesday":
                if (startTime != null)
                    settings.setWednesdayStart(startTime);
                if (endTime != null)
                    settings.setWednesdayEnd(endTime);
                if (isActive != null)
                    settings.setWednesdayActive(isActive);
                break;
            case "thursday":
                if (startTime != null)
                    settings.setThursdayStart(startTime);
                if (endTime != null)
                    settings.setThursdayEnd(endTime);
                if (isActive != null)
                    settings.setThursdayActive(isActive);
                break;
            case "friday":
                if (startTime != null)
                    settings.setFridayStart(startTime);
                if (endTime != null)
                    settings.setFridayEnd(endTime);
                if (isActive != null)
                    settings.setFridayActive(isActive);
                break;
        }

        settings.setUpdatedAt(LocalDateTime.now());
        return settingsRepository.save(settings);
    }

    /**
     * Update notification preferences
     */
    public CounselorSettings updateNotifications(User counselor, Boolean notifyAppointments, Boolean notifyHighRisk,
            Boolean notifyForumReports) {
        CounselorSettings settings = getOrCreateSettings(counselor);

        if (notifyAppointments != null) {
            settings.setNotifyAppointments(notifyAppointments);
        }
        if (notifyHighRisk != null) {
            settings.setNotifyHighRisk(notifyHighRisk);
        }
        if (notifyForumReports != null) {
            settings.setNotifyForumReports(notifyForumReports);
        }

        settings.setUpdatedAt(LocalDateTime.now());
        return settingsRepository.save(settings);
    }

    /**
     * Save settings entity directly
     */
    public CounselorSettings save(CounselorSettings settings) {
        settings.setUpdatedAt(LocalDateTime.now());
        return settingsRepository.save(settings);
    }
}

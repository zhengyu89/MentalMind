package com.example.MentalMind.service;

import com.example.MentalMind.model.StudentSettings;
import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.StudentSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StudentSettingsService {

    @Autowired
    private StudentSettingsRepository settingsRepository;

    /**
     * Get existing settings for a student, or create new default settings if none
     * exist.
     */
    public StudentSettings getOrCreateSettings(User student) {
        Optional<StudentSettings> existing = settingsRepository.findByStudent(student);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new settings with defaults
        StudentSettings settings = new StudentSettings(student);
        return settingsRepository.save(settings);
    }

    /**
     * Update profile information
     */
    public StudentSettings updateProfile(User student, String bio, String faculty, String course, String yearOfStudy,
            String profilePhotoUrl) {
        StudentSettings settings = getOrCreateSettings(student);

        if (bio != null) {
            settings.setBio(bio);
        }
        if (faculty != null) {
            settings.setFaculty(faculty);
        }
        if (course != null) {
            settings.setCourse(course);
        }
        if (yearOfStudy != null) {
            settings.setYearOfStudy(yearOfStudy);
        }
        if (profilePhotoUrl != null) {
            settings.setProfilePhotoUrl(profilePhotoUrl);
        }

        settings.setUpdatedAt(LocalDateTime.now());
        return settingsRepository.save(settings);
    }

    /**
     * Update profile photo only
     */
    public StudentSettings updatePhoto(User student, String profilePhotoUrl) {
        StudentSettings settings = getOrCreateSettings(student);
        settings.setProfilePhotoUrl(profilePhotoUrl);
        settings.setUpdatedAt(LocalDateTime.now());
        return settingsRepository.save(settings);
    }

    /**
     * Save settings entity directly
     */
    public StudentSettings save(StudentSettings settings) {
        settings.setUpdatedAt(LocalDateTime.now());
        return settingsRepository.save(settings);
    }
}

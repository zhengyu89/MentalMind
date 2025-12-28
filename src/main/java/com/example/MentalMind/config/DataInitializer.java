package com.example.MentalMind.config;

import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.User;
import com.example.MentalMind.model.Appointment;
import com.example.MentalMind.repository.MoodEntryRepository;
import com.example.MentalMind.repository.UserRepository;
import com.example.MentalMind.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only insert if users don't exist
        User student = null;
        User counselor = null;

        if (userRepository.findByEmail("student@example.com").isEmpty()) {
            student = new User("student@example.com", "password123", "student");
            student.setFullName("John Student");
            student = userRepository.save(student);

            // Initialize mock mood data for the past week
            initializeMoodData(student);
        } else {
            student = userRepository.findByEmail("student@example.com").get();
        }

        if (userRepository.findByEmail("counselor@example.com").isEmpty()) {
            counselor = new User("counselor@example.com", "password123", "counselor");
            counselor.setFullName("Dr. Anya Sharma");
            counselor = userRepository.save(counselor);
        } else {
            counselor = userRepository.findByEmail("counselor@example.com").get();
        }

        // Create additional counselors if they don't exist
        User counselor2 = null;
        User counselor3 = null;

        if (userRepository.findByEmail("counselor2@example.com").isEmpty()) {
            counselor2 = new User("counselor2@example.com", "password123", "counselor");
            counselor2.setFullName("Dr. Mike Johnson");
            counselor2 = userRepository.save(counselor2);
        } else {
            counselor2 = userRepository.findByEmail("counselor2@example.com").get();
        }

        if (userRepository.findByEmail("counselor3@example.com").isEmpty()) {
            counselor3 = new User("counselor3@example.com", "password123", "counselor");
            counselor3.setFullName("Dr. Sarah Wilson");
            counselor3 = userRepository.save(counselor3);
        } else {
            counselor3 = userRepository.findByEmail("counselor3@example.com").get();
        }

        // Initialize sample appointments
        if (appointmentRepository.findByStudentOrderByAppointmentDateTimeDesc(student).isEmpty()) {
            initializeAppointments(student, counselor, counselor2, counselor3);
        }
    }

    private void initializeMoodData(User student) {
        // Mock mood scores for the past 7 days (today going backwards)
        // Scores: 1=Terrible, 2=Bad, 3=Okay, 4=Good, 5=Great
        int[] moodScores = { 5, 4, 3, 4, 5, 2, 4 }; // Today, yesterday, 2 days ago, etc.
        String[] notes = {
                "Feeling great today! Had a productive study session.",
                "Good day overall, completed my assignments.",
                "Just an average day, nothing special.",
                "Had a nice lunch with friends.",
                "Aced my presentation!",
                "Stressed about upcoming exams.",
                "Relaxing weekend, feeling refreshed."
        };

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < moodScores.length; i++) {
            MoodEntry entry = new MoodEntry();
            entry.setUser(student);
            entry.setMoodScore(moodScores[i]);
            entry.setNotes(notes[i]);
            // Set the date to i days ago, at noon to avoid timezone issues
            entry.setCreatedAt(now.minusDays(i).withHour(12).withMinute(0).withSecond(0));
            moodEntryRepository.save(entry);
        }
    }

    private void initializeAppointments(User student, User counselor, User counselor2, User counselor3) {
        LocalDateTime now = LocalDateTime.now();

        // Upcoming appointment - Approved
        Appointment appointment1 = new Appointment();
        appointment1.setStudent(student);
        appointment1.setCounselor(counselor);
        appointment1.setAppointmentDateTime(now.plusDays(3).withHour(10).withMinute(0).withSecond(0));
        appointment1.setReason("Anxiety management and stress relief techniques");
        appointment1.setStatus("APPROVED");
        appointment1.setCreatedAt(now.minusDays(1));
        appointmentRepository.save(appointment1);

        // Upcoming appointment - Pending
        Appointment appointment2 = new Appointment();
        appointment2.setStudent(student);
        appointment2.setCounselor(counselor2);
        appointment2.setAppointmentDateTime(now.plusDays(7).withHour(14).withMinute(0).withSecond(0));
        appointment2.setReason("Depression support and coping strategies");
        appointment2.setStatus("PENDING");
        appointment2.setCreatedAt(now.minusHours(6));
        appointmentRepository.save(appointment2);

        // Past appointment - Completed
        Appointment appointment3 = new Appointment();
        appointment3.setStudent(student);
        appointment3.setCounselor(counselor);
        appointment3.setAppointmentDateTime(now.minusDays(7).withHour(11).withMinute(0).withSecond(0));
        appointment3.setReason("Initial consultation and assessment");
        appointment3.setStatus("COMPLETED");
        appointment3.setCreatedAt(now.minusDays(10));
        appointmentRepository.save(appointment3);

        // Past appointment - Completed
        Appointment appointment4 = new Appointment();
        appointment4.setStudent(student);
        appointment4.setCounselor(counselor3);
        appointment4.setAppointmentDateTime(now.minusDays(14).withHour(15).withMinute(30).withSecond(0));
        appointment4.setReason("Follow-up session and progress review");
        appointment4.setStatus("COMPLETED");
        appointment4.setCreatedAt(now.minusDays(17));
        appointmentRepository.save(appointment4);
    }
}

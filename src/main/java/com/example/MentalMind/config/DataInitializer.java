package com.example.MentalMind.config;

import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.User;
import com.example.MentalMind.model.Feedback;
import com.example.MentalMind.model.CounselorResponse;
import com.example.MentalMind.model.Appointment;
import com.example.MentalMind.repository.MoodEntryRepository;
import com.example.MentalMind.repository.UserRepository;
import com.example.MentalMind.repository.FeedbackRepository;
import com.example.MentalMind.repository.CounselorResponseRepository;
import com.example.MentalMind.repository.AppointmentRepository;
import com.example.MentalMind.service.ResourceService;
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
    private ResourceService resourceService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private CounselorResponseRepository counselorResponseRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize users
        User student = initializeStudent();
        User counselor = initializeCounselor("counselor@example.com", "Dr. Anya Sharma");
        User counselor2 = initializeCounselor("counselor2@example.com", "Dr. Mike Johnson");
        User counselor3 = initializeCounselor("counselor3@example.com", "Dr. Sarah Wilson");

        // Initialize sample feedback and counselor responses
        if (student != null && counselor != null) {
            initializeFeedback(student, counselor);
        }

        // Initialize sample appointments
        if (student != null && counselor != null && counselor2 != null && counselor3 != null) {
            if (appointmentRepository.findByStudentOrderByAppointmentDateTimeDesc(student).isEmpty()) {
                initializeAppointments(student, counselor, counselor2, counselor3);
            }
        }

        // Remove initial seeded resources (if present) to clear the starter data.
        if (!resourceService.getAllResources().isEmpty()) {
            removeSeededResources();
        }
    }

    private User initializeStudent() {
        if (userRepository.findByEmail("student@example.com").isEmpty()) {
            User student = new User("student@example.com", "password123", "student");
            student.setFullName("John Student");
            student = userRepository.save(student);

            // Initialize mock mood data for the past week
            initializeMoodData(student);
            System.out.println("[DataInitializer] Inserted sample student: student@example.com");
            return student;
        } else {
            System.out.println("[DataInitializer] Sample student already exists.");
            return userRepository.findByEmail("student@example.com").orElse(null);
        }
    }

    private User initializeCounselor(String email, String fullName) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User counselor = new User(email, "password123", "counselor");
            counselor.setFullName(fullName);
            counselor = userRepository.save(counselor);
            System.out.println("[DataInitializer] Inserted sample counselor: " + email);
            return counselor;
        } else {
            System.out.println("[DataInitializer] Sample counselor already exists: " + email);
            return userRepository.findByEmail(email).orElse(null);
        }
    }

    private void initializeFeedback(User student, User counselor) {
        // Insert sample feedback and counselor responses if not present
        if (!feedbackRepository.findAll().stream().anyMatch(f -> "Need help with study plan".equals(f.getSubject()))) {
            try {
                // Create two sample feedback entries from the student
                Feedback fb1 = new Feedback();
                fb1.setUser(student);
                fb1.setType("general");
                fb1.setSubject("Need help with study plan");
                fb1.setDetails(
                        "I'm struggling to manage my study time and would appreciate guidance on creating a study plan.");
                fb1.setStatus("pending");
                feedbackRepository.save(fb1);

                Feedback fb2 = new Feedback();
                fb2.setUser(student);
                fb2.setType("report");
                fb2.setSubject("Issue with course materials");
                fb2.setDetails(
                        "Some lecture slides are missing from the resources section. Please advise how to access them.");
                fb2.setStatus("reviewed");
                feedbackRepository.save(fb2);

                System.out.println("[DataInitializer] Inserted 2 sample feedback entries.");

                // Add a counselor response to the first feedback if one with similar message
                // isn't present
                boolean needResponse = counselorResponseRepository.findAll().stream()
                        .noneMatch(r -> r.getMessage() != null
                                && r.getMessage().contains("I can help build a study plan"));

                if (needResponse) {
                    CounselorResponse resp = new CounselorResponse();
                    resp.setFeedback(fb1);
                    resp.setCounselor(counselor);
                    resp.setResponseType("advice");
                    resp.setMessage(
                            "Thanks for reaching out — I can help build a study plan. Let's schedule a short session this week.");
                    counselorResponseRepository.save(resp);

                    // Mark feedback as responded
                    fb1.setStatus("responded");
                    feedbackRepository.save(fb1);

                    System.out.println(
                            "[DataInitializer] Inserted a sample counselor response for feedback id=" + fb1.getId());
                }
            } catch (Exception ex) {
                System.out.println("[DataInitializer] Error inserting sample feedback: " + ex.getMessage());
            }
        } else {
            System.out.println("[DataInitializer] Sample feedback subject already present; skipping sample insert.");
        }
    }

    private void removeSeededResources() {
        java.util.List<com.example.MentalMind.model.Resource> list = resourceService.getAllResources();
        for (com.example.MentalMind.model.Resource r : list) {
            String t = r.getTitle() == null ? "" : r.getTitle().trim();
            if ("Coping Strategies for Stress".equalsIgnoreCase(t) || "Mindfulness for Beginners".equalsIgnoreCase(t)) {
                try {
                    resourceService.deactivateResource(r.getId());
                } catch (Exception e) {
                    // ignore any errors during cleanup
                }
            }
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

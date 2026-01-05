package com.example.MentalMind.config;

import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.User;
import com.example.MentalMind.model.Feedback;
import com.example.MentalMind.model.CounselorResponse;
import com.example.MentalMind.repository.MoodEntryRepository;
import com.example.MentalMind.repository.UserRepository;
import com.example.MentalMind.repository.FeedbackRepository;
import com.example.MentalMind.repository.CounselorResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MoodEntryRepository moodEntryRepository;
    
    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private CounselorResponseRepository counselorResponseRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only insert if users don't exist
        if (userRepository.findByEmail("student@example.com").isEmpty()) {
            User student = new User("student@example.com", "password123", "student");
            student.setFullName("John Student");
            userRepository.save(student);

            // Initialize mock mood data for the past week
            initializeMoodData(student);
            System.out.println("[DataInitializer] Inserted sample student: student@example.com");
        } else {
            System.out.println("[DataInitializer] Sample student already exists.");
        }

        User counselorUser;
        if (userRepository.findByEmail("counselor@example.com").isEmpty()) {
            counselorUser = new User("counselor@example.com", "password123", "counselor");
            counselorUser.setFullName("Jane Counselor");
            userRepository.save(counselorUser);
            System.out.println("[DataInitializer] Inserted sample counselor: counselor@example.com");
        } else {
            counselorUser = userRepository.findByEmail("counselor@example.com").orElse(null);
            System.out.println("[DataInitializer] Sample counselor already exists.");
        }

        // Insert sample feedback and counselor responses if the example subjects are not present
        if (!feedbackRepository.findAll().stream().anyMatch(f -> "Need help with study plan".equals(f.getSubject()))) {
            try {
                // Create two sample feedback entries from the student
                Feedback fb1 = new Feedback();
                fb1.setUser(userRepository.findByEmail("student@example.com").orElse(null));
                fb1.setType("general");
                fb1.setSubject("Need help with study plan");
                fb1.setDetails("I'm struggling to manage my study time and would appreciate guidance on creating a study plan.");
                fb1.setStatus("pending");
                feedbackRepository.save(fb1);

                Feedback fb2 = new Feedback();
                fb2.setUser(userRepository.findByEmail("student@example.com").orElse(null));
                fb2.setType("report");
                fb2.setSubject("Issue with course materials");
                fb2.setDetails("Some lecture slides are missing from the resources section. Please advise how to access them.");
                fb2.setStatus("reviewed");
                feedbackRepository.save(fb2);

                System.out.println("[DataInitializer] Inserted 2 sample feedback entries.");

                // Add a counselor response to the first feedback if one with similar message isn't present
                boolean needResponse = true;
                try {
                    needResponse = counselorResponseRepository.findAll().stream()
                            .noneMatch(r -> r.getMessage() != null && r.getMessage().contains("I can help build a study plan"));
                } catch (Exception ignored) {}

                if (counselorUser != null && needResponse) {
                    CounselorResponse resp = new CounselorResponse();
                    resp.setFeedback(fb1);
                    resp.setCounselor(counselorUser);
                    resp.setResponseType("advice");
                    resp.setMessage("Thanks for reaching out — I can help build a study plan. Let's schedule a short session this week.");
                    counselorResponseRepository.save(resp);

                    // Mark feedback as responded
                    fb1.setStatus("responded");
                    feedbackRepository.save(fb1);

                    System.out.println("[DataInitializer] Inserted a sample counselor response for feedback id=" + fb1.getId());
                }
            } catch (Exception ex) {
                System.out.println("[DataInitializer] Error inserting sample feedback: " + ex.getMessage());
            }
        } else {
            System.out.println("[DataInitializer] Sample feedback subject already present; skipping sample insert.");
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
}

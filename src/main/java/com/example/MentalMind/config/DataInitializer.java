package com.example.MentalMind.config;

import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.MoodEntryRepository;
import com.example.MentalMind.repository.UserRepository;
import com.example.MentalMind.service.ResourceService;
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
    private ResourceService resourceService;

    @Override
    public void run(String... args) throws Exception {
        // Only insert if users don't exist
        if (userRepository.findByEmail("student@example.com").isEmpty()) {
            User student = new User("student@example.com", "password123", "student");
            student.setFullName("John Student");
            userRepository.save(student);

            // Initialize mock mood data for the past week
            initializeMoodData(student);
        }

        if (userRepository.findByEmail("counselor@example.com").isEmpty()) {
            User counselor = new User("counselor@example.com", "password123", "counselor");
            counselor.setFullName("Jane Counselor");
            userRepository.save(counselor);
        }

        // Remove initial seeded resources (if present) to clear the starter data.
        // This will only deactivate resources that match the original seed titles.
        if (!resourceService.getAllResources().isEmpty()) {
            removeSeededResources();
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

    private void initializeResources() {
        // Coping Strategies for Stress
        resourceService.createResource(
            "Coping Strategies for Stress",
            "guide",
            "Learn practical techniques to manage daily stress and build resilience.",
            "<div class=\"prose dark:prose-invert max-w-none\"><h4>Introduction</h4><p>Stress is a normal part of life, especially for students. Learning effective coping strategies can help you manage stress and maintain your mental well-being.</p><h4>Key Strategies</h4><ol><li><strong>Deep Breathing</strong> - Practice the 4-7-8 technique: inhale for 4 seconds, hold for 7, exhale for 8.</li><li><strong>Physical Activity</strong> - Even a 10-minute walk can significantly reduce stress hormones.</li><li><strong>Time Management</strong> - Break large tasks into smaller, manageable chunks.</li><li><strong>Social Support</strong> - Talk to friends, family, or counselors about your feelings.</li><li><strong>Mindfulness</strong> - Practice being present in the moment without judgment.</li></ol><h4>Try This Exercise</h4><p>The 5-4-3-2-1 Grounding Technique:</p><ul><li>Name 5 things you can see</li><li>Name 4 things you can touch</li><li>Name 3 things you can hear</li><li>Name 2 things you can smell</li><li>Name 1 thing you can taste</li></ul></div>",
            "spa",
            "teal-400",
            "green-500",
            "teal"
        );

        // Mindfulness for Beginners
        resourceService.createResource(
            "Mindfulness for Beginners",
            "article",
            "Start your mindfulness journey with simple exercises you can do anywhere.",
            "<div class=\"prose dark:prose-invert max-w-none\"><h4>What is Mindfulness?</h4><p>Mindfulness is the practice of being fully present and engaged in the current moment, without judgment. It can help reduce stress, improve focus, and enhance emotional regulation.</p><h4>Simple Exercises to Start</h4><ol><li><strong>Mindful Breathing</strong> - Focus on your breath for 5 minutes. Notice the sensation of air entering and leaving your body.</li><li><strong>Body Scan</strong> - Slowly move your attention through different parts of your body, noticing any tension or sensations.</li><li><strong>Mindful Eating</strong> - Eat one meal slowly, savoring each bite and noticing the flavors and textures.</li></ol><h4>Tips for Success</h4><ul><li>Start with just 5 minutes a day</li><li>Be patient with yourself</li><li>Practice at the same time each day</li><li>Use guided meditations if helpful</li></ul></div>",
            "self_improvement",
            "amber-400",
            "orange-500",
            "amber"
        );
    }
}

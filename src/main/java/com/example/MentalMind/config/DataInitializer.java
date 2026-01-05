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

        // Initialize sample resources
        if (resourceService.getAllResources().isEmpty()) {
            initializeResources();
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
        // 1. Understanding Anxiety
        resourceService.createResource(
            "Understanding Anxiety",
            "article",
            "Learn about anxiety symptoms and evidence-based treatments.",
            "<div class=\"prose dark:prose-invert max-w-none\"><h4>What is Anxiety?</h4><p>Anxiety is a normal emotion characterized by worry, fear, or apprehension about future events. However, when anxiety becomes excessive and interferes with daily life, it may be a sign of an anxiety disorder.</p><h4>Common Symptoms</h4><ul><li>Persistent worry or fear</li><li>Difficulty concentrating</li><li>Sleep disturbances</li><li>Physical tension or restlessness</li><li>Rapid heartbeat or shortness of breath</li></ul><h4>Evidence-Based Treatments</h4><ol><li><strong>Cognitive Behavioral Therapy (CBT)</strong> - Identifies and challenges negative thought patterns</li><li><strong>Exposure Therapy</strong> - Gradual exposure to anxiety triggers</li><li><strong>Medication</strong> - Such as SSRIs prescribed by healthcare providers</li><li><strong>Lifestyle Changes</strong> - Regular exercise, meditation, and sleep hygiene</li></ol></div>",
            "heart",
            "red-400",
            "pink-500",
            "red",
            "anxiety",
            "/assets/download (2).jpeg"
        );

        // 2. Stress Management Techniques
        resourceService.createResource(
            "Stress Management Techniques",
            "guide",
            "Practical techniques to manage stress and improve mental health.",
            "<div class=\"prose dark:prose-invert max-w-none\"><h4>Introduction</h4><p>Stress is a natural response to challenges, but chronic stress can negatively impact your health. Learn practical techniques to manage stress effectively.</p><h4>Key Techniques</h4><ol><li><strong>Deep Breathing Exercises</strong> - The 4-7-8 technique can activate your relaxation response</li><li><strong>Progressive Muscle Relaxation</strong> - Tense and release each muscle group systematically</li><li><strong>Time Management</strong> - Prioritize tasks and break them into manageable chunks</li><li><strong>Physical Activity</strong> - Exercise releases endorphins and reduces stress hormones</li><li><strong>Maintain Healthy Routines</strong> - Consistent sleep, nutrition, and social connection</li></ol><h4>30-Minute Stress Relief Plan</h4><ul><li>5 min: Deep breathing exercises</li><li>10 min: Light exercise or walk</li><li>10 min: Journaling or meditation</li><li>5 min: Gratitude reflection</li></ul></div>",
            "brain",
            "blue-400",
            "cyan-500",
            "blue",
            "stress",
            "/assets/3145195-article-tips-to-reduce-stress-5a8c75818e1b6e0036533c47-922c3155e9c846eaa7447c75030b2c13.png"
        );

        // 3. Mental Health Resources and Support (with YouTube video)
        resourceService.createResource(
            "What Is Anxiety Really? Stress, Anxiety, and Worry",
            "video",
            "Comprehensive guide to mental health resources and getting support.",
            "<div class=\"prose dark:prose-invert max-w-none\"><h4>Mental Health Support is Available</h4><p>If you're struggling with your mental health, know that support is available. Watch this helpful video about mental health resources.</p><h4>Video Resource</h4><p><a href=\"https://youtu.be/db3K8b3ftaY?si=AUjbZHPItFkiO82b\" target=\"_blank\" class=\"text-blue-500 hover:underline\">Mental Health Support Guide</a></p><h4>Types of Support</h4><ul><li><strong>Professional Therapy</strong> - Individual or group counseling with licensed therapists</li><li><strong>Support Groups</strong> - Connect with others facing similar challenges</li><li><strong>Crisis Services</strong> - 24/7 hotlines for immediate mental health crises</li><li><strong>Campus Resources</strong> - Free counseling and wellness programs at your institution</li><li><strong>Peer Support</strong> - Lean on trusted friends and family members</li></ul><h4>Remember</h4><p>Reaching out for help is a sign of strength, not weakness. Your mental health matters.</p></div>",
            "video",
            "purple-400",
            "indigo-500",
            "purple",
            "anxiety",
            null
        );

        // 4. Building Healthy Relationships
        resourceService.createResource(
            "Building Healthy Relationships",
            "guide",
            "Learn how to develop and maintain meaningful, healthy relationships.",
            "<div class=\"prose dark:prose-invert max-w-none\"><h4>The Importance of Relationships</h4><p>Strong relationships are fundamental to mental health and well-being. They provide support, reduce stress, and give life meaning.</p><h4>Characteristics of Healthy Relationships</h4><ol><li><strong>Trust and Honesty</strong> - Open communication without fear of judgment</li><li><strong>Respect and Boundaries</strong> - Honoring each other's limits and individuality</li><li><strong>Mutual Support</strong> - Being there for each other during good and difficult times</li><li><strong>Effective Communication</strong> - Active listening and expressing feelings clearly</li><li><strong>Shared Values</strong> - Common goals and understanding of what matters</li></ol><h4>How to Build Stronger Relationships</h4><ul><li>Practice active listening without immediately giving advice</li><li>Be vulnerable and share your true feelings</li><li>Show appreciation and gratitude regularly</li><li>Spend quality time together</li><li>Address conflicts promptly and respectfully</li><li>Maintain individual identities and interests</li></ul></div>",
            "people",
            "green-400",
            "emerald-500",
            "green",
            "relationships",
            "/assets/download (1).jpeg"
        );

        // 5. Sleep and Mental Health
        resourceService.createResource(
            "Sleep and Mental Health",
            "article",
            "Discover the crucial connection between sleep quality and mental well-being.",
            "<div class=\"prose dark:prose-invert max-w-none\"><h4>Why Sleep Matters for Mental Health</h4><p>Sleep is essential for emotional regulation, cognitive function, and mental health. Poor sleep can worsen anxiety, depression, and stress.</p><h4>The Sleep-Mental Health Connection</h4><ul><li><strong>Emotional Processing</strong> - Sleep helps your brain process emotions and experiences</li><li><strong>Memory Consolidation</strong> - Sleep strengthens memory formation and learning</li><li><strong>Stress Hormone Regulation</strong> - Adequate sleep reduces cortisol and adrenaline levels</li><li><strong>Immune Function</strong> - Sleep supports the immune system and reduces inflammation</li></ul><h4>Tips for Better Sleep</h4><ol><li><strong>Maintain a Schedule</strong> - Go to bed and wake up at consistent times</li><li><strong>Create a Sleep Environment</strong> - Keep your bedroom dark, cool, and quiet</li><li><strong>Limit Screen Time</strong> - Avoid screens 1 hour before bed</li><li><strong>Avoid Caffeine Late</strong> - Limit caffeine intake after 2 PM</li><li><strong>Relaxation Techniques</strong> - Try meditation, breathing exercises, or gentle stretching</li><li><strong>Physical Activity</strong> - Exercise during the day but not close to bedtime</li></ol><h4>When to Seek Help</h4><p>If you consistently struggle with sleep despite good sleep hygiene, consult a healthcare provider.</p></div>",
            "moon",
            "indigo-400",
            "blue-500",
            "indigo",
            "mindfulness",
            "/assets/download.jpeg"
        );
    }
}

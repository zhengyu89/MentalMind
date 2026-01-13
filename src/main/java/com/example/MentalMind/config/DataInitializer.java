package com.example.MentalMind.config;

import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.User;
import com.example.MentalMind.model.Feedback;
import com.example.MentalMind.model.CounselorResponse;
import com.example.MentalMind.model.Appointment;
import com.example.MentalMind.model.SelfAssessmentResult;
import com.example.MentalMind.model.CounselorSettings;
import com.example.MentalMind.model.LearningModule;
import com.example.MentalMind.model.LearningMaterial;
import com.example.MentalMind.model.MaterialType;
import com.example.MentalMind.model.ForumPost;
import com.example.MentalMind.model.ForumComment;
import com.example.MentalMind.model.ForumPostLike;
import com.example.MentalMind.model.ForumPostFlag;
import com.example.MentalMind.repository.MoodEntryRepository;
import com.example.MentalMind.repository.UserRepository;
import com.example.MentalMind.repository.FeedbackRepository;
import com.example.MentalMind.repository.CounselorResponseRepository;
import com.example.MentalMind.repository.AppointmentRepository;
import com.example.MentalMind.repository.SelfAssessmentRepository;
import com.example.MentalMind.repository.CounselorSettingsRepository;
import com.example.MentalMind.repository.LearningModuleRepository;
import com.example.MentalMind.repository.LearningMaterialRepository;
import com.example.MentalMind.repository.ForumPostRepository;
import com.example.MentalMind.repository.ForumCommentRepository;
import com.example.MentalMind.repository.ForumPostLikeRepository;
import com.example.MentalMind.repository.ForumPostFlagRepository;
import com.example.MentalMind.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final String LOG_PREFIX = "[DataInitializer]";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Autowired
    private SelfAssessmentRepository selfAssessmentRepository;

    @Autowired
    private CounselorSettingsRepository counselorSettingsRepository;

    @Autowired
    private LearningModuleRepository learningModuleRepository;

    @Autowired
    private LearningMaterialRepository learningMaterialRepository;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Autowired
    private ForumPostLikeRepository forumPostLikeRepository;

    @Autowired
    private ForumPostFlagRepository forumPostFlagRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(LOG_PREFIX + " ========== Starting Data Initialization ==========");

        // Initialize users
        User student = initializeStudent();
        User counselor = initializeCounselor("counselor@example.com", "Dr. Anya Sharma");
        User counselor2 = initializeCounselor("counselor2@example.com", "Dr. Mike Johnson");
        User counselor3 = initializeCounselor("counselor3@example.com", "Dr. Sarah Wilson");

        // Initialize 20 students with self-assessment results
        List<User> students = initializeStudents();

        // Initialize sample feedback and counselor responses
        if (student != null && counselor != null) {
            initializeFeedback(student, counselor);
        } else {
            System.out.println(LOG_PREFIX + " Skipping feedback initialization (student or counselor is null)");
        }

        // Initialize sample appointments
        if (student != null && counselor != null && counselor2 != null && counselor3 != null) {
            if (appointmentRepository.findByStudentOrderByAppointmentDateTimeAsc(student).isEmpty()) {
                initializeAppointments(student, counselor, counselor2, counselor3);
            } else {
                System.out.println(LOG_PREFIX + " Sample appointments already exist; skipping.");
            }
        } else {
            System.out.println(LOG_PREFIX + " Skipping appointments initialization (one or more users are null)");
        }

        // Initialize sample resources if none exist
        if (resourceService.getAllResources().isEmpty()) {
            initializeResources();
        } else {
            System.out.println(LOG_PREFIX + " Resources already exist; skipping resource initialization.");
        }

        // Initialize counselor settings
        initializeCounselorSettings(counselor);
        initializeCounselorSettings(counselor2);
        initializeCounselorSettings(counselor3);

        // Initialize learning modules and materials
        if (counselor != null) {
            initializeLearningModules(counselor);
        } else {
            System.out.println(LOG_PREFIX + " Skipping learning modules initialization (counselor is null)");
        }

        // Initialize forum posts if empty
        if (students != null && !students.isEmpty()) {
            initializeForumPosts(students);
            // Seed some sample likes after posts are created
            initializeForumLikes(students);
            // Seed some sample flags on posts
            initializeForumFlags(students, counselor, counselor2);
        } else {
            System.out.println(LOG_PREFIX + " Skipping forum posts initialization (no students available)");
        }

        System.out.println(LOG_PREFIX + " ========== Data Initialization Complete ==========");
    }

    private User initializeStudent() {
        System.out.println(LOG_PREFIX + " Checking for student user...");
        if (userRepository.findByEmail("student@example.com").isEmpty()) {
            User student = new User("student@example.com", passwordEncoder.encode("password123"), "student");
            student.setFullName("John Student");
            student = userRepository.save(student);

            // Initialize mock mood data for the past week
            initializeMoodData(student);
            System.out.println(
                    LOG_PREFIX + " ✓ Inserted sample student: student@example.com (ID: " + student.getId() + ")");
            return student;
        } else {
            System.out.println(LOG_PREFIX + " Sample student already exists.");
            return userRepository.findByEmail("student@example.com").orElse(null);
        }
    }

    private User initializeCounselor(String email, String fullName) {
        System.out.println(LOG_PREFIX + " Checking for counselor: " + email + "...");
        if (userRepository.findByEmail(email).isEmpty()) {
            User counselor = new User(email, passwordEncoder.encode("password123"), "counselor");
            counselor.setFullName(fullName);
            counselor = userRepository.save(counselor);
            System.out.println(
                    LOG_PREFIX + " ✓ Inserted sample counselor: " + email + " (ID: " + counselor.getId() + ")");
            return counselor;
        } else {
            System.out.println(LOG_PREFIX + " Sample counselor already exists: " + email);
            return userRepository.findByEmail(email).orElse(null);
        }
    }

    private void initializeFeedback(User student, User counselor) {
        System.out.println(LOG_PREFIX + " Checking for sample feedback...");
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
                System.out.println(
                        LOG_PREFIX + " ✓ Inserted feedback #1: 'Need help with study plan' (ID: " + fb1.getId() + ")");

                Feedback fb2 = new Feedback();
                fb2.setUser(student);
                fb2.setType("report");
                fb2.setSubject("Issue with course materials");
                fb2.setDetails(
                        "Some lecture slides are missing from the resources section. Please advise how to access them.");
                fb2.setStatus("reviewed");
                feedbackRepository.save(fb2);
                System.out.println(LOG_PREFIX + " ✓ Inserted feedback #2: 'Issue with course materials' (ID: "
                        + fb2.getId() + ")");

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

                    System.out.println(LOG_PREFIX + " ✓ Inserted counselor response for feedback ID: " + fb1.getId());
                }
            } catch (Exception ex) {
                System.out.println(LOG_PREFIX + " ✗ Error inserting sample feedback: " + ex.getMessage());
            }
        } else {
            System.out.println(LOG_PREFIX + " Sample feedback already exists; skipping.");
        }
    }

    private void initializeMoodData(User student) {
        System.out.println(LOG_PREFIX + " Initializing mood data for student...");
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
        System.out.println(LOG_PREFIX + " ✓ Inserted " + moodScores.length + " mood entries for student.");
    }

    private void initializeAppointments(User student, User counselor, User counselor2, User counselor3) {
        System.out.println(LOG_PREFIX + " Initializing sample appointments...");
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
        System.out.println(LOG_PREFIX + " ✓ Inserted appointment #1: APPROVED with " + counselor.getFullName());

        // Upcoming appointment - Pending
        Appointment appointment2 = new Appointment();
        appointment2.setStudent(student);
        appointment2.setCounselor(counselor2);
        appointment2.setAppointmentDateTime(now.plusDays(7).withHour(14).withMinute(0).withSecond(0));
        appointment2.setReason("Depression support and coping strategies");
        appointment2.setStatus("PENDING");
        appointment2.setCreatedAt(now.minusHours(6));
        appointmentRepository.save(appointment2);
        System.out.println(LOG_PREFIX + " ✓ Inserted appointment #2: PENDING with " + counselor2.getFullName());

        // Past appointment - Completed
        Appointment appointment3 = new Appointment();
        appointment3.setStudent(student);
        appointment3.setCounselor(counselor);
        appointment3.setAppointmentDateTime(now.minusDays(7).withHour(11).withMinute(0).withSecond(0));
        appointment3.setReason("Initial consultation and assessment");
        appointment3.setStatus("COMPLETED");
        appointment3.setCreatedAt(now.minusDays(10));
        appointmentRepository.save(appointment3);
        System.out.println(LOG_PREFIX + " ✓ Inserted appointment #3: COMPLETED with " + counselor.getFullName());

        // Past appointment - Completed
        Appointment appointment4 = new Appointment();
        appointment4.setStudent(student);
        appointment4.setCounselor(counselor3);
        appointment4.setAppointmentDateTime(now.minusDays(14).withHour(15).withMinute(30).withSecond(0));
        appointment4.setReason("Follow-up session and progress review");
        appointment4.setStatus("COMPLETED");
        appointment4.setCreatedAt(now.minusDays(17));
        appointmentRepository.save(appointment4);
        System.out.println(LOG_PREFIX + " ✓ Inserted appointment #4: COMPLETED with " + counselor3.getFullName());

        System.out.println(LOG_PREFIX + " ✓ Total: 4 appointments created.");
    }

    private void initializeResources() {
        System.out.println(LOG_PREFIX + " Initializing sample resources...");

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
                "/assets/download (2).jpeg");
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Understanding Anxiety'");

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
                "/assets/3145195-article-tips-to-reduce-stress-5a8c75818e1b6e0036533c47-922c3155e9c846eaa7447c75030b2c13.png");
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Stress Management Techniques'");

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
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'What Is Anxiety Really? Stress, Anxiety, and Worry'");

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
                "/assets/download (1).jpeg");
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Building Healthy Relationships'");

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
                "/assets/download.jpeg");
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Sleep and Mental Health'");

        // 6. Mindfulness Meditation Guide
        resourceService.createResource(
                "Mindfulness Meditation Guide",
                "guide",
                "A beginner's guide to mindfulness meditation for stress relief and mental clarity.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>What is Mindfulness?</h4><p>Mindfulness is the practice of being fully present and engaged in the current moment, without judgment. It helps reduce stress and improve mental clarity.</p><h4>Getting Started</h4><ol><li><strong>Find a Quiet Space</strong> - Choose a calm environment free from distractions</li><li><strong>Set a Timer</strong> - Start with 5-10 minutes and gradually increase</li><li><strong>Focus on Breathing</strong> - Pay attention to each inhale and exhale</li><li><strong>Notice Thoughts</strong> - When your mind wanders, gently bring focus back</li><li><strong>Be Patient</strong> - Mindfulness is a skill that improves with practice</li></ol><h4>Benefits of Regular Practice</h4><ul><li>Reduced anxiety and stress</li><li>Improved focus and concentration</li><li>Better emotional regulation</li><li>Enhanced self-awareness</li><li>Lower blood pressure</li></ul></div>",
                "sparkles",
                "teal-400",
                "cyan-500",
                "teal",
                "mindfulness",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Mindfulness Meditation Guide'");

        // 7. Understanding Depression
        resourceService.createResource(
                "Understanding Depression",
                "article",
                "Learn about depression symptoms, causes, and treatment options.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>What is Depression?</h4><p>Depression is more than just feeling sad. It's a serious mental health condition that affects how you feel, think, and handle daily activities.</p><h4>Common Signs</h4><ul><li>Persistent sad, anxious, or empty mood</li><li>Loss of interest in activities once enjoyed</li><li>Fatigue and decreased energy</li><li>Difficulty concentrating or making decisions</li><li>Changes in sleep patterns</li><li>Appetite or weight changes</li><li>Feelings of hopelessness or worthlessness</li></ul><h4>Treatment Options</h4><ol><li><strong>Psychotherapy</strong> - Talk therapy with a mental health professional</li><li><strong>Medication</strong> - Antidepressants prescribed by a doctor</li><li><strong>Lifestyle Changes</strong> - Exercise, healthy diet, regular sleep</li><li><strong>Support Groups</strong> - Connecting with others who understand</li></ol><h4>When to Seek Help</h4><p>If symptoms persist for more than two weeks, reach out to a healthcare provider.</p></div>",
                "cloud",
                "gray-400",
                "slate-500",
                "gray",
                "depression",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Understanding Depression'");

        // 8. Academic Stress Management
        resourceService.createResource(
                "Academic Stress Management",
                "guide",
                "Strategies for students to manage academic pressure effectively.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>Understanding Academic Stress</h4><p>Academic stress is common among students and can stem from exams, assignments, and performance expectations. Learning to manage it is crucial for success.</p><h4>Key Strategies</h4><ol><li><strong>Break Tasks into Chunks</strong> - Large projects feel less overwhelming when divided</li><li><strong>Create a Study Schedule</strong> - Plan your study time and stick to it</li><li><strong>Use Active Learning</strong> - Engage with material through practice and discussion</li><li><strong>Take Regular Breaks</strong> - Use techniques like the Pomodoro method</li><li><strong>Prioritize Tasks</strong> - Focus on high-impact assignments first</li></ol><h4>Self-Care During Exams</h4><ul><li>Get adequate sleep before exams</li><li>Eat nutritious meals</li><li>Exercise to reduce tension</li><li>Practice relaxation techniques</li><li>Avoid cramming at the last minute</li></ul><h4>Seeking Support</h4><p>Don't hesitate to reach out to professors, tutors, or counselors when you need help.</p></div>",
                "academic-cap",
                "amber-400",
                "yellow-500",
                "amber",
                "stress",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Academic Stress Management'");

        // 9. Building Self-Esteem
        resourceService.createResource(
                "Building Self-Esteem",
                "article",
                "Learn how to develop a healthy sense of self-worth and confidence.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>What is Self-Esteem?</h4><p>Self-esteem is how you perceive and value yourself. Healthy self-esteem means having a balanced, realistic view of your worth and abilities.</p><h4>Signs of Low Self-Esteem</h4><ul><li>Negative self-talk and self-criticism</li><li>Difficulty accepting compliments</li><li>Fear of failure or trying new things</li><li>Comparing yourself unfavorably to others</li><li>Seeking constant validation from others</li></ul><h4>Building Healthy Self-Esteem</h4><ol><li><strong>Practice Self-Compassion</strong> - Treat yourself with the kindness you'd show a friend</li><li><strong>Challenge Negative Thoughts</strong> - Question and reframe critical self-talk</li><li><strong>Set Achievable Goals</strong> - Celebrate small victories</li><li><strong>Focus on Strengths</strong> - Acknowledge what you do well</li><li><strong>Surround Yourself with Positivity</strong> - Choose supportive relationships</li></ol><h4>Daily Affirmations</h4><p>Start each day with positive statements about yourself. Examples: 'I am capable,' 'I deserve respect,' 'I am worthy of love.'</p></div>",
                "star",
                "yellow-400",
                "orange-500",
                "yellow",
                "mindfulness",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Building Self-Esteem'");

        // 10. The Power of Gratitude
        resourceService.createResource(
                "The Power of Gratitude",
                "article",
                "Discover how practicing gratitude can improve your mental health.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>Why Gratitude Matters</h4><p>Research shows that practicing gratitude can significantly improve mental health by shifting focus from what's lacking to what's present in your life.</p><h4>Benefits of Gratitude</h4><ul><li>Increased happiness and life satisfaction</li><li>Better sleep quality</li><li>Reduced symptoms of depression</li><li>Improved relationships</li><li>Enhanced resilience during difficult times</li></ul><h4>How to Practice Gratitude</h4><ol><li><strong>Keep a Gratitude Journal</strong> - Write 3 things you're grateful for daily</li><li><strong>Express Thanks</strong> - Tell people you appreciate them</li><li><strong>Mindful Appreciation</strong> - Notice and savor positive moments</li><li><strong>Gratitude Letters</strong> - Write to someone who impacted your life</li><li><strong>Reframe Challenges</strong> - Find lessons in difficult situations</li></ol><h4>Start Today</h4><p>Take a moment right now to think of three things you're grateful for.</p></div>",
                "gift",
                "rose-400",
                "pink-500",
                "rose",
                "mindfulness",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'The Power of Gratitude'");

        // 11. Time Management for Students
        resourceService.createResource(
                "Time Management for Students",
                "guide",
                "Master time management skills to balance academics and personal life.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>Why Time Management Matters</h4><p>Effective time management reduces stress, improves academic performance, and creates space for personal activities and self-care.</p><h4>Core Principles</h4><ol><li><strong>Set Clear Goals</strong> - Define what you want to achieve daily, weekly, monthly</li><li><strong>Prioritize Tasks</strong> - Use the Eisenhower Matrix (urgent vs. important)</li><li><strong>Plan Ahead</strong> - Schedule your week in advance</li><li><strong>Avoid Procrastination</strong> - Break tasks into smaller, manageable steps</li><li><strong>Learn to Say No</strong> - Protect your time from unnecessary commitments</li></ol><h4>Useful Techniques</h4><ul><li><strong>Pomodoro Technique</strong> - 25 minutes focus, 5 minutes break</li><li><strong>Time Blocking</strong> - Assign specific time slots to tasks</li><li><strong>Two-Minute Rule</strong> - If it takes less than 2 minutes, do it now</li><li><strong>Weekly Review</strong> - Reflect on what worked and what didn't</li></ul><h4>Balance is Key</h4><p>Schedule time for rest and recreation—they boost productivity in the long run.</p></div>",
                "clock",
                "sky-400",
                "blue-500",
                "sky",
                "stress",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Time Management for Students'");

        // 12. Overcoming Social Anxiety
        resourceService.createResource(
                "Overcoming Social Anxiety",
                "article",
                "Understand social anxiety and learn strategies to overcome it.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>What is Social Anxiety?</h4><p>Social anxiety is an intense fear of social situations due to worries about being judged, embarrassed, or rejected by others.</p><h4>Common Symptoms</h4><ul><li>Fear of meeting new people or public speaking</li><li>Avoiding social situations</li><li>Physical symptoms (blushing, sweating, trembling)</li><li>Excessive worry before social events</li><li>Difficulty making eye contact</li></ul><h4>Coping Strategies</h4><ol><li><strong>Challenge Negative Thoughts</strong> - Question assumptions about how others perceive you</li><li><strong>Gradual Exposure</strong> - Start with low-stakes social situations</li><li><strong>Focus Outward</strong> - Shift attention from yourself to others</li><li><strong>Practice Social Skills</strong> - Role-play conversations to build confidence</li><li><strong>Deep Breathing</strong> - Use breathing exercises to calm physical symptoms</li></ol><h4>Professional Help</h4><p>Cognitive Behavioral Therapy (CBT) is highly effective for social anxiety. Consider seeking professional support.</p></div>",
                "users",
                "violet-400",
                "purple-500",
                "violet",
                "anxiety",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Overcoming Social Anxiety'");

        // 13. Understanding Panic Attacks
        resourceService.createResource(
                "Understanding Panic Attacks",
                "article",
                "Learn what panic attacks are and how to manage them effectively.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>What is a Panic Attack?</h4><p>A panic attack is a sudden episode of intense fear that triggers severe physical reactions. They can be frightening but are not dangerous.</p><h4>Common Symptoms</h4><ul><li>Rapid heartbeat or palpitations</li><li>Shortness of breath</li><li>Chest pain or discomfort</li><li>Dizziness or lightheadedness</li><li>Feeling of unreality or detachment</li><li>Fear of losing control or dying</li><li>Sweating and trembling</li></ul><h4>What To Do During a Panic Attack</h4><ol><li><strong>Remember It Will Pass</strong> - Panic attacks typically peak within 10 minutes</li><li><strong>Focus on Breathing</strong> - Slow, deep breaths (in for 4, hold for 4, out for 4)</li><li><strong>Ground Yourself</strong> - Use the 5-4-3-2-1 technique</li><li><strong>Stay Where You Are</strong> - If safe, stay put until it passes</li><li><strong>Challenge Thoughts</strong> - Remind yourself you are safe</li></ol><h4>Preventing Future Attacks</h4><p>Regular exercise, adequate sleep, limiting caffeine, and managing stress can help reduce the frequency of panic attacks.</p></div>",
                "bolt",
                "orange-400",
                "red-500",
                "orange",
                "anxiety",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Understanding Panic Attacks'");

        // 14. Recognizing and Preventing Burnout
        resourceService.createResource(
                "Recognizing and Preventing Burnout",
                "guide",
                "Identify the signs of burnout and learn how to prevent and recover from it.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>What is Burnout?</h4><p>Burnout is a state of chronic stress leading to physical and emotional exhaustion, cynicism, and feelings of ineffectiveness.</p><h4>Warning Signs</h4><ul><li>Constant fatigue and low energy</li><li>Feeling detached or cynical about work/studies</li><li>Decreased productivity and performance</li><li>Difficulty concentrating</li><li>Physical symptoms (headaches, sleep problems)</li><li>Neglecting personal needs</li></ul><h4>Prevention Strategies</h4><ol><li><strong>Set Boundaries</strong> - Learn to say no to excessive demands</li><li><strong>Take Regular Breaks</strong> - Step away from work regularly</li><li><strong>Prioritize Self-Care</strong> - Exercise, sleep, and nutrition matter</li><li><strong>Seek Support</strong> - Talk to friends, family, or professionals</li><li><strong>Reconnect with Purpose</strong> - Remember why you started</li></ol><h4>Recovery</h4><p>If you're already burned out, consider taking time off, reducing workload, and focusing on activities that restore your energy.</p></div>",
                "fire",
                "red-400",
                "orange-500",
                "red",
                "stress",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Recognizing and Preventing Burnout'");

        // 15. Developing Emotional Intelligence
        resourceService.createResource(
                "Developing Emotional Intelligence",
                "article",
                "Enhance your emotional intelligence for better relationships and well-being.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>What is Emotional Intelligence?</h4><p>Emotional Intelligence (EQ) is the ability to recognize, understand, manage, and effectively use emotions in yourself and others.</p><h4>The Five Components</h4><ol><li><strong>Self-Awareness</strong> - Recognizing your own emotions and their impact</li><li><strong>Self-Regulation</strong> - Managing emotions in healthy ways</li><li><strong>Motivation</strong> - Using emotions to stay focused on goals</li><li><strong>Empathy</strong> - Understanding and sharing others' feelings</li><li><strong>Social Skills</strong> - Building and maintaining relationships</li></ol><h4>Benefits of High EQ</h4><ul><li>Better relationships and communication</li><li>Improved mental health</li><li>Enhanced leadership abilities</li><li>Greater resilience in facing challenges</li><li>Better decision-making</li></ul><h4>How to Improve EQ</h4><ul><li>Practice mindfulness to increase self-awareness</li><li>Pause before reacting to strong emotions</li><li>Listen actively and empathetically</li><li>Seek feedback from others</li><li>Reflect on your emotional responses</li></ul></div>",
                "light-bulb",
                "amber-400",
                "yellow-500",
                "amber",
                "relationships",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Developing Emotional Intelligence'");

        // 16. Effective Communication Skills
        resourceService.createResource(
                "Effective Communication Skills",
                "guide",
                "Learn essential communication skills for healthier interactions.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>Why Communication Matters</h4><p>Effective communication is the foundation of healthy relationships and reduces misunderstandings, conflicts, and stress.</p><h4>Key Communication Skills</h4><ol><li><strong>Active Listening</strong> - Give full attention without planning your response</li><li><strong>Clear Expression</strong> - State your thoughts directly and respectfully</li><li><strong>Non-Verbal Awareness</strong> - Be mindful of body language and tone</li><li><strong>Empathy</strong> - Try to understand others' perspectives</li><li><strong>Assertiveness</strong> - Express needs without being aggressive</li></ol><h4>I-Statements</h4><p>Use 'I' statements instead of 'You' statements to express feelings without blame. For example: 'I feel overwhelmed when deadlines pile up' instead of 'You always give too much work.'</p><h4>Conflict Resolution</h4><ul><li>Stay calm and take a break if needed</li><li>Focus on the issue, not the person</li><li>Seek to understand before being understood</li><li>Look for win-win solutions</li><li>Know when to agree to disagree</li></ul></div>",
                "chat-bubble-left-right",
                "cyan-400",
                "teal-500",
                "cyan",
                "relationships",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Effective Communication Skills'");

        // 17. Practicing Self-Compassion
        resourceService.createResource(
                "Practicing Self-Compassion",
                "article",
                "Learn how to be kinder to yourself and cultivate self-compassion.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>What is Self-Compassion?</h4><p>Self-compassion means treating yourself with the same kindness, understanding, and support you would offer a good friend during difficult times.</p><h4>Three Elements of Self-Compassion</h4><ol><li><strong>Self-Kindness</strong> - Being gentle with yourself rather than harshly critical</li><li><strong>Common Humanity</strong> - Recognizing that suffering and imperfection are part of the human experience</li><li><strong>Mindfulness</strong> - Holding painful feelings in balanced awareness</li></ol><h4>Why It Matters</h4><ul><li>Reduces anxiety and depression</li><li>Increases emotional resilience</li><li>Improves motivation and well-being</li><li>Leads to healthier habits and self-care</li></ul><h4>How to Practice</h4><ul><li>Notice self-critical thoughts and reframe them kindly</li><li>Write a compassionate letter to yourself</li><li>Place your hand on your heart during difficult moments</li><li>Ask: 'What would I say to a friend in this situation?'</li><li>Practice loving-kindness meditation</li></ul></div>",
                "heart",
                "pink-400",
                "rose-500",
                "pink",
                "mindfulness",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Practicing Self-Compassion'");

        // 18. Exercise and Mental Health
        resourceService.createResource(
                "Exercise and Mental Health",
                "article",
                "Discover how physical activity positively impacts your mental well-being.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>The Mind-Body Connection</h4><p>Regular physical activity is one of the most effective ways to improve mental health. Exercise releases endorphins and reduces stress hormones.</p><h4>Mental Health Benefits</h4><ul><li>Reduces symptoms of anxiety and depression</li><li>Improves mood and energy levels</li><li>Enhances sleep quality</li><li>Boosts self-esteem and confidence</li><li>Improves cognitive function and memory</li><li>Provides healthy coping mechanism for stress</li></ul><h4>Getting Started</h4><ol><li><strong>Start Small</strong> - Even 10 minutes of walking helps</li><li><strong>Find Enjoyable Activities</strong> - Dance, swim, hike, or play sports</li><li><strong>Set Realistic Goals</strong> - Aim for 150 minutes per week</li><li><strong>Make It Social</strong> - Exercise with friends for added motivation</li><li><strong>Be Consistent</strong> - Regular activity is more beneficial than intense but sporadic workouts</li></ol><h4>Types of Exercise</h4><ul><li>Aerobic: Walking, running, cycling, swimming</li><li>Strength: Weight training, bodyweight exercises</li><li>Mind-Body: Yoga, tai chi, stretching</li></ul></div>",
                "running",
                "lime-400",
                "green-500",
                "lime",
                "mindfulness",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Exercise and Mental Health'");

        // 19. Digital Wellness and Screen Time
        resourceService.createResource(
                "Digital Wellness and Screen Time",
                "guide",
                "Learn how to maintain a healthy relationship with technology.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>The Impact of Technology</h4><p>While technology offers many benefits, excessive screen time can negatively affect mental health, sleep, and relationships.</p><h4>Signs of Unhealthy Tech Use</h4><ul><li>Difficulty disconnecting from devices</li><li>Feeling anxious without your phone</li><li>Neglecting in-person relationships</li><li>Comparing yourself to others on social media</li><li>Sleep disruption from late-night scrolling</li></ul><h4>Digital Wellness Tips</h4><ol><li><strong>Set Screen Time Limits</strong> - Use built-in tools to monitor usage</li><li><strong>Create Tech-Free Zones</strong> - Keep devices out of bedrooms</li><li><strong>Schedule Digital Detox</strong> - Take regular breaks from screens</li><li><strong>Curate Your Feed</strong> - Follow accounts that inspire, not drain</li><li><strong>Turn Off Notifications</strong> - Reduce constant interruptions</li></ol><h4>Mindful Technology Use</h4><ul><li>Be intentional about when and why you use devices</li><li>Prioritize face-to-face interactions</li><li>Practice being present without documenting everything</li></ul></div>",
                "device-phone-mobile",
                "fuchsia-400",
                "violet-500",
                "fuchsia",
                "mindfulness",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Digital Wellness and Screen Time'");

        // 20. Crisis Support and Helplines
        resourceService.createResource(
                "Crisis Support and Helplines",
                "article",
                "Important mental health crisis resources and how to get immediate help.",
                "<div class=\"prose dark:prose-invert max-w-none\"><h4>When to Seek Immediate Help</h4><p>If you or someone you know is in crisis, experiencing suicidal thoughts, or feeling unsafe, reach out immediately.</p><h4>Warning Signs of Crisis</h4><ul><li>Talking about wanting to die or harm oneself</li><li>Feeling hopeless or having no reason to live</li><li>Feeling trapped or in unbearable pain</li><li>Giving away prized possessions</li><li>Extreme mood swings or agitation</li><li>Withdrawing from friends and activities</li></ul><h4>Crisis Resources (Malaysia)</h4><ul><li><strong>Befrienders KL</strong>: 03-7956 8145 (24 hours)</li><li><strong>Talian Kasih</strong>: 15999 (24 hours)</li><li><strong>Malaysian Mental Health Association</strong>: 03-2780 6803</li></ul><h4>How to Help Someone in Crisis</h4><ol><li><strong>Stay Calm</strong> - Listen without judgment</li><li><strong>Take It Seriously</strong> - Never dismiss their feelings</li><li><strong>Don't Leave Them Alone</strong> - Stay with them or connect them with help</li><li><strong>Remove Dangerous Items</strong> - If possible, remove access to means of harm</li><li><strong>Seek Professional Help</strong> - Encourage them to call a helpline or go to emergency services</li></ol><h4>You Are Not Alone</h4><p>Help is available 24/7. Reaching out is a sign of strength, not weakness.</p></div>",
                "phone",
                "red-400",
                "rose-500",
                "red",
                "depression",
                null);
        System.out.println(LOG_PREFIX + " ✓ Created resource: 'Crisis Support and Helplines'");

        System.out.println(LOG_PREFIX + " ✓ Total: 20 resources created.");
    }

    private List<User> initializeStudents() {
        System.out.println(LOG_PREFIX + " Initializing 20 students with self-assessments...");
        List<User> students = new ArrayList<>();
        Random random = new Random(42); // Fixed seed for reproducible data

        // Malaysian student names
        String[] names = {
                "Ahmad Farhan bin Ismail",
                "Nurul Aisyah binti Rahman",
                "Muhammad Haziq bin Abdullah",
                "Siti Nurhaliza binti Hassan",
                "Lee Wei Ming",
                "Tan Mei Ling",
                "Rajesh Kumar a/l Subramaniam",
                "Priya Devi a/p Krishnan",
                "Muhammad Aiman bin Yusof",
                "Fatimah Zahra binti Omar",
                "Wong Jia Wei",
                "Lim Shi Ting",
                "Aravind a/l Muthu",
                "Kavitha a/p Rajan",
                "Nur Syafiqah binti Mohd Ali",
                "Ahmad Danial bin Karim",
                "Chen Xiao Yu",
                "Ng Hui Wen",
                "Ganesh a/l Vellu",
                "Deepa a/p Naidu"
        };

        for (int i = 0; i < 20; i++) {
            String email = "student" + (i + 1) + "@example.com";

            // Check if student already exists
            if (userRepository.findByEmail(email).isPresent()) {
                System.out.println(LOG_PREFIX + " Student " + email + " already exists; skipping.");
                students.add(userRepository.findByEmail(email).get());
                continue;
            }

            // Create new student with encoded password
            User student = new User(email, passwordEncoder.encode("password123"), "student");
            student.setFullName(names[i]);
            student = userRepository.save(student);
            students.add(student);

            // Generate self-assessment results for this student
            int assessmentCount = 3 + random.nextInt(8); // 3 to 10 assessments per student
            initializeSelfAssessments(student, assessmentCount, random);

            System.out.println(LOG_PREFIX + " ✓ Created student: " + names[i] + " (" + email + ") with "
                    + assessmentCount + " assessments");
        }

        System.out.println(LOG_PREFIX + " ✓ Total: 20 students initialized with self-assessments.");
        return students;
    }

    private void initializeSelfAssessments(User student, int count, Random random) {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < count; i++) {
            // Spread assessments over the past 60 days
            int daysAgo = random.nextInt(60);
            LocalDateTime completedAt = now.minusDays(daysAgo)
                    .withHour(8 + random.nextInt(12)) // Between 8 AM and 8 PM
                    .withMinute(random.nextInt(60))
                    .withSecond(0);

            // Generate a realistic PSS-10 score (0-40)
            // Using a distribution that creates varied stress levels
            int score;
            int pattern = random.nextInt(10);
            if (pattern < 4) {
                // 40% LOW stress (0-13)
                score = random.nextInt(14);
            } else if (pattern < 8) {
                // 40% MODERATE stress (14-26)
                score = 14 + random.nextInt(13);
            } else {
                // 20% HIGH stress (27-40)
                score = 27 + random.nextInt(14);
            }

            // Determine stress level based on score
            String stressLevel;
            if (score <= 13) {
                stressLevel = "LOW";
            } else if (score <= 26) {
                stressLevel = "MODERATE";
            } else {
                stressLevel = "HIGH";
            }

            SelfAssessmentResult result = new SelfAssessmentResult();
            result.setUser(student);
            result.setScore(score);
            result.setStressLevel(stressLevel);
            result.setCompletedAt(completedAt);

            selfAssessmentRepository.save(result);
        }
    }

    private void initializeCounselorSettings(User counselor) {
        if (counselor == null) {
            return;
        }

        // Check if settings already exist
        if (counselorSettingsRepository.findByCounselor(counselor).isPresent()) {
            System.out.println(LOG_PREFIX + " Settings already exist for counselor: " + counselor.getEmail());
            return;
        }

        System.out.println(LOG_PREFIX + " Creating settings for counselor: " + counselor.getEmail());

        CounselorSettings settings = new CounselorSettings(counselor);
        settings.setBio(
                "Licensed counselor with expertise in helping students navigate mental health challenges. Committed to providing a safe, supportive environment for all students.");
        settings.setSpecialization("Anxiety, Depression, Academic Stress");

        // Default availability: Mon-Thu 9 AM - 5 PM, Fri 9 AM - 3 PM
        settings.setMondayStart("09:00");
        settings.setMondayEnd("17:00");
        settings.setMondayActive(true);

        settings.setTuesdayStart("09:00");
        settings.setTuesdayEnd("17:00");
        settings.setTuesdayActive(true);

        settings.setWednesdayStart("09:00");
        settings.setWednesdayEnd("17:00");
        settings.setWednesdayActive(true);

        settings.setThursdayStart("09:00");
        settings.setThursdayEnd("17:00");
        settings.setThursdayActive(true);

        settings.setFridayStart("09:00");
        settings.setFridayEnd("15:00");
        settings.setFridayActive(true);

        // Enable all notifications by default
        settings.setNotifyAppointments(true);
        settings.setNotifyHighRisk(true);
        settings.setNotifyForumReports(true);

        counselorSettingsRepository.save(settings);
        System.out.println(LOG_PREFIX + " ✓ Created settings for counselor: " + counselor.getEmail());
    }

    private void initializeLearningModules(User counselor) {
        System.out.println(LOG_PREFIX + " Initializing learning modules and materials...");

        // Check if modules already exist
        if (!learningModuleRepository.findByIsActiveTrue().isEmpty()) {
            System.out.println(LOG_PREFIX + " Learning modules already exist; skipping.");
            return;
        }

        // Module 1: Mental Health Basics
        LearningModule module1 = new LearningModule();
        module1.setTitle("Mental Health Basics");
        module1.setDescription(
                "Understand the fundamentals of mental health, mental illness, and the importance of mental wellness in daily life.");
        module1.setCreatedBy(counselor.getId());
        module1.setIsActive(true);
        learningModuleRepository.save(module1);

        // Add materials to Module 1
        LearningMaterial material1_1 = new LearningMaterial();
        material1_1.setModule(module1);
        material1_1.setTitle("What is Mental Health?");
        material1_1.setMaterialType(MaterialType.DOCUMENT);
        material1_1.setContent(
                "<h3>What is Mental Health?</h3><p>Mental health refers to your psychological, emotional, and social well-being. It affects how you think, feel, and act in your daily life.</p><h4>Key Areas of Mental Health:</h4><ul><li><strong>Emotional Health</strong> - Your ability to feel and express your emotions appropriately</li><li><strong>Psychological Health</strong> - Your ability to think clearly and make good decisions</li><li><strong>Social Health</strong> - Your relationships and interactions with others</li></ul><h4>Why It Matters:</h4><p>Good mental health helps you cope with stress, realize your potential, and contribute meaningfully to your community. Mental health is just as important as physical health.</p>");
        material1_1.setCreatedBy(counselor.getId());
        material1_1.setIsActive(true);
        learningMaterialRepository.save(material1_1);

        LearningMaterial material1_2 = new LearningMaterial();
        material1_2.setModule(module1);
        material1_2.setTitle("Mental Health Basics Video");
        material1_2.setMaterialType(MaterialType.VIDEO);
        material1_2.setContent("https://www.youtube.com/embed/g-Zj-vkqH8k");
        material1_2.setCreatedBy(counselor.getId());
        material1_2.setIsActive(true);
        learningMaterialRepository.save(material1_2);

        System.out.println(LOG_PREFIX + " ✓ Created module: 'Mental Health Basics' with 2 materials");

        // Module 2: Stress Management
        LearningModule module2 = new LearningModule();
        module2.setTitle("Stress Management");
        module2.setDescription("Learn practical techniques to identify, manage, and reduce stress in your life.");
        module2.setCreatedBy(counselor.getId());
        module2.setIsActive(true);
        learningModuleRepository.save(module2);

        // Add materials to Module 2
        LearningMaterial material2_1 = new LearningMaterial();
        material2_1.setModule(module2);
        material2_1.setTitle("Understanding Stress");
        material2_1.setMaterialType(MaterialType.DOCUMENT);
        material2_1.setContent(
                "<h3>Understanding Stress</h3><p>Stress is your body's natural reaction to any demand or threat. While some stress is normal, excessive stress can impact your health and well-being.</p><h4>Types of Stress:</h4><ul><li><strong>Acute Stress</strong> - Short-term stress from specific events (exams, presentations)</li><li><strong>Chronic Stress</strong> - Long-term stress that persists over weeks or months</li></ul><h4>Physical Symptoms of Stress:</h4><ul><li>Headaches and muscle tension</li><li>Sleep disturbances</li><li>Difficulty concentrating</li><li>Changes in appetite</li><li>Fatigue and low energy</li></ul><h4>Emotional Symptoms:</h4><ul><li>Irritability and mood swings</li><li>Anxiety or panic</li><li>Feeling overwhelmed</li></ul>");
        material2_1.setCreatedBy(counselor.getId());
        material2_1.setIsActive(true);
        learningMaterialRepository.save(material2_1);

        LearningMaterial material2_2 = new LearningMaterial();
        material2_2.setModule(module2);
        material2_2.setTitle("Quick Stress Relief Techniques");
        material2_2.setMaterialType(MaterialType.DOCUMENT);
        material2_2.setContent(
                "<h3>5-Minute Stress Relief Techniques</h3><h4>1. Deep Breathing (2-3 minutes)</h4><p>Sit comfortably and breathe in slowly for 4 counts, hold for 4, and exhale for 4. Repeat 5-10 times.</p><h4>2. Progressive Muscle Relaxation (3-5 minutes)</h4><p>Tense and release each muscle group from toes to head. Hold each for 3-5 seconds.</p><h4>3. Mindful Walking (5 minutes)</h4><p>Walk slowly while focusing on each step and your surroundings. Pay attention to sensations.</p><h4>4. The 5-4-3-2-1 Grounding Technique</h4><p>Notice 5 things you see, 4 you can touch, 3 you hear, 2 you smell, 1 you taste.</p>");
        material2_2.setCreatedBy(counselor.getId());
        material2_2.setIsActive(true);
        learningMaterialRepository.save(material2_2);

        System.out.println(LOG_PREFIX + " ✓ Created module: 'Stress Management' with 2 materials");

        // Module 3: Anxiety and Depression
        LearningModule module3 = new LearningModule();
        module3.setTitle("Anxiety and Depression");
        module3.setDescription(
                "Understand anxiety and depression, recognize symptoms, and learn evidence-based strategies for managing these conditions.");
        module3.setCreatedBy(counselor.getId());
        module3.setIsActive(true);
        learningModuleRepository.save(module3);

        // Add materials to Module 3
        LearningMaterial material3_1 = new LearningMaterial();
        material3_1.setModule(module3);
        material3_1.setTitle("What is Anxiety?");
        material3_1.setMaterialType(MaterialType.DOCUMENT);
        material3_1.setContent(
                "<h3>Understanding Anxiety Disorders</h3><p>Anxiety is a feeling of unease, worry, or fear. While occasional anxiety is normal, anxiety disorders involve persistent, excessive worry that interferes with daily activities.</p><h4>Common Anxiety Disorders:</h4><ul><li><strong>Generalized Anxiety Disorder (GAD)</strong> - Persistent worry about many aspects of life</li><li><strong>Social Anxiety</strong> - Intense fear in social situations</li><li><strong>Panic Disorder</strong> - Sudden, intense panic attacks</li><li><strong>Phobias</strong> - Extreme fear of specific objects or situations</li></ul><h4>Symptoms of Anxiety:</h4><ul><li>Rapid heartbeat and shortness of breath</li><li>Sweating, trembling, or dizziness</li><li>Difficulty concentrating</li><li>Sleep problems</li><li>Muscle tension</li></ul><h4>Treatment Options:</h4><ol><li>Cognitive Behavioral Therapy (CBT)</li><li>Exposure therapy</li><li>Medication (prescribed by healthcare providers)</li><li>Lifestyle changes (exercise, meditation, sleep)</li></ol>");
        material3_1.setCreatedBy(counselor.getId());
        material3_1.setIsActive(true);
        learningMaterialRepository.save(material3_1);

        LearningMaterial material3_2 = new LearningMaterial();
        material3_2.setModule(module3);
        material3_2.setTitle("Managing Depression");
        material3_2.setMaterialType(MaterialType.DOCUMENT);
        material3_2.setContent(
                "<h3>Understanding and Managing Depression</h3><p>Depression is a mood disorder characterized by persistent sadness, loss of interest, and difficulty functioning. It's more than just feeling sad; it's a medical condition that requires attention.</p><h4>Signs of Depression:</h4><ul><li>Persistent sadness or empty mood</li><li>Loss of interest in activities you used to enjoy</li><li>Changes in appetite or weight</li><li>Sleep disturbances (insomnia or oversleeping)</li><li>Fatigue and low energy</li><li>Difficulty concentrating or making decisions</li><li>Feelings of worthlessness or guilt</li><li>Thoughts of death or suicide</li></ul><h4>Steps to Manage Depression:</h4><ol><li><strong>Seek Professional Help</strong> - Talk to a counselor, therapist, or doctor</li><li><strong>Stay Active</strong> - Physical activity can boost mood</li><li><strong>Maintain Routines</strong> - Regular sleep and meal times help</li><li><strong>Social Connection</strong> - Reach out to friends and family</li><li><strong>Self-Care</strong> - Prioritize activities that bring you joy</li><li><strong>Avoid Isolation</strong> - Depression thrives in isolation</li></ol><p><strong>If you're having thoughts of suicide, reach out immediately to a crisis hotline or emergency services.</strong></p>");
        material3_2.setCreatedBy(counselor.getId());
        material3_2.setIsActive(true);
        learningMaterialRepository.save(material3_2);

        System.out.println(LOG_PREFIX + " ✓ Created module: 'Anxiety and Depression' with 2 materials");

        // Module 4: Self-Care and Wellness
        LearningModule module4 = new LearningModule();
        module4.setTitle("Self-Care and Wellness");
        module4.setDescription(
                "Learn about self-care practices and build healthy habits that support your mental and physical wellness.");
        module4.setCreatedBy(counselor.getId());
        module4.setIsActive(true);
        learningModuleRepository.save(module4);

        // Add materials to Module 4
        LearningMaterial material4_1 = new LearningMaterial();
        material4_1.setModule(module4);
        material4_1.setTitle("Self-Care Essentials");
        material4_1.setMaterialType(MaterialType.DOCUMENT);
        material4_1.setContent(
                "<h3>Self-Care Essentials for Mental Health</h3><p>Self-care is any activity you deliberately do to maintain or improve your physical, mental, or emotional health. It's not selfish; it's necessary.</p><h4>Physical Self-Care:</h4><ul><li>Regular exercise (at least 30 minutes, 3-5 times per week)</li><li>Adequate sleep (7-9 hours per night)</li><li>Balanced nutrition and hydration</li><li>Limiting alcohol and avoiding drugs</li></ul><h4>Mental/Emotional Self-Care:</h4><ul><li>Meditation or mindfulness practices</li><li>Journaling your thoughts and feelings</li><li>Creative expression (art, music, writing)</li><li>Setting boundaries and saying no</li></ul><h4>Social Self-Care:</h4><ul><li>Spending time with loved ones</li><li>Engaging in community activities</li><li>Volunteering or helping others</li><li>Maintaining meaningful relationships</li></ul><h4>Spiritual Self-Care:</h4><ul><li>Prayer or spiritual practices if that resonates with you</li><li>Connecting with nature</li><li>Finding purpose and meaning</li></ul>");
        material4_1.setCreatedBy(counselor.getId());
        material4_1.setIsActive(true);
        learningMaterialRepository.save(material4_1);

        LearningMaterial material4_2 = new LearningMaterial();
        material4_2.setModule(module4);
        material4_2.setTitle("Sleep Hygiene for Better Rest");
        material4_2.setMaterialType(MaterialType.DOCUMENT);
        material4_2.setContent(
                "<h3>Sleep Hygiene: Your Guide to Better Rest</h3><p>Sleep is crucial for mental health, memory, immune function, and overall well-being. Good sleep hygiene can significantly improve sleep quality.</p><h4>Sleep Hygiene Tips:</h4><ol><li><strong>Maintain a Regular Schedule</strong> - Go to bed and wake up at the same time daily, even weekends</li><li><strong>Create a Restful Environment</strong> - Keep bedroom dark, cool (65-68°F), and quiet</li><li><strong>Limit Screen Time</strong> - Avoid screens 1-2 hours before bed (blue light interferes with melatonin)</li><li><strong>Watch Your Caffeine Intake</strong> - Avoid caffeine after 2 PM</li><li><strong>Exercise Regularly</strong> - But not within 3 hours of bedtime</li><li><strong>Avoid Heavy Meals Before Bed</strong> - Eat dinner 2-3 hours before sleep</li><li><strong>Limit Naps</strong> - Keep daytime naps to 20-30 minutes maximum</li><li><strong>Manage Stress</strong> - Use relaxation techniques or meditation</li><li><strong>Avoid Alcohol and Smoking</strong> - Both interfere with sleep quality</li></ol><h4>If You Still Can't Sleep:</h4><p>If you lie awake for more than 20 minutes, get up and do a quiet, non-stimulating activity until you feel sleepy.</p>");
        material4_2.setCreatedBy(counselor.getId());
        material4_2.setIsActive(true);
        learningMaterialRepository.save(material4_2);

        System.out.println(LOG_PREFIX + " ✓ Created module: 'Self-Care and Wellness' with 2 materials");

        System.out.println(LOG_PREFIX + " ✓ Total: 4 learning modules with 8 materials created.");
    }

    private void initializeForumPosts(List<User> students) {
        System.out.println(LOG_PREFIX + " Checking for forum posts...");
        
        if (forumPostRepository.count() > 0) {
            System.out.println(LOG_PREFIX + " Forum posts already exist; skipping.");
            return;
        }

        String[] categories = {"anxiety", "stress", "depression", "academic", "general"};
        String[][] posts = {
            {
                "Struggling with exam anxiety",
                "I have a major exam coming up next week and the anxiety is overwhelming. My mind goes blank when I try to study. Does anyone have tips for managing test anxiety? I'd really appreciate some support.",
                "anxiety"
            },
            {
                "How to deal with work-life balance",
                "Between classes, assignments, and part-time work, I'm constantly stressed. I feel like I'm always behind on something. Any advice on managing multiple responsibilities without burning out?",
                "stress"
            },
            {
                "Feeling lost and unmotivated",
                "I've been feeling really down lately and struggling to find motivation to do anything. Even things I used to enjoy don't seem fun anymore. Is this something others have experienced? How did you get through it?",
                "depression"
            },
            {
                "Tips for better time management",
                "I struggle with procrastination and managing my time effectively. I always end up cramming for exams and rushing assignments. Would love to hear how others organize their schedules. What works best for you?",
                "academic"
            },
            {
                "Sleep problems affecting my studies",
                "My sleep schedule is all messed up and it's affecting my ability to focus in classes. I've tried everything but I'm still exhausted. Anyone else dealing with this? What helped you?",
                "general"
            },
            {
                "Dealing with social anxiety in class",
                "I get really anxious when I have to speak up in class or present in front of people. My heart races and I feel like everyone's judging me. Is there anyone else who deals with this? How do you cope?",
                "anxiety"
            },
            {
                "Overwhelming amount of assignments",
                "This semester feels impossible. I have so many projects and papers due that I don't know where to start. The stress is affecting my mental health. How do you all handle heavy workloads?",
                "stress"
            },
            {
                "Dealing with loneliness at university",
                "Even though I'm surrounded by people, I feel really alone and disconnected. It's hard to make genuine connections and I feel isolated. Does anyone else feel this way?",
                "depression"
            },
            {
                "Study habits that actually work",
                "I've been using the Pomodoro technique (25 mins study, 5 mins break) and it's been a game-changer for my focus and productivity. What study methods have worked best for you all? Share your secrets!",
                "academic"
            },
            {
                "Mental health resources on campus",
                "I wanted to share that our campus counseling center has some amazing free resources and support groups. If anyone is struggling, please reach out. Mental health is just as important as physical health.",
                "general"
            }
        };

        String[][] commentTexts = {
            {"Thanks for sharing! I've been feeling the same way.", "You're not alone in this. Have you tried talking to a counselor?", "I find deep breathing exercises help me a lot."},
            {"I totally understand! Try breaking tasks into smaller chunks.", "Setting boundaries is important. Don't be afraid to say no sometimes.", "Maybe try scheduling specific time blocks for each activity?"},
            {"Please reach out to someone if you need help. You matter!", "I went through something similar. It does get better with time.", "Have you considered joining some student groups or activities?"},
            {"Pomodoro technique works wonders for me!", "I use a planner app to stay organized. Game changer!", "Try studying in short bursts rather than marathon sessions.", "Also, eliminate distractions like your phone during study time."},
            {"Avoid screens before bed. It really helps!", "Try maintaining a consistent sleep schedule even on weekends.", "Melatonin supplements helped me, but talk to a doctor first."},
            {"I get this too! Remember most people are focused on themselves, not judging you.", "Practice in front of a mirror or with friends first.", "The more you do it, the easier it gets. You've got this!"},
            {"Make a priority list and tackle the most urgent first.", "Don't forget to take breaks! Burnout is real.", "Maybe talk to your professors about extensions if needed?"},
            {"Try joining clubs related to your interests. That's how I made friends.", "Quality over quantity with friendships. It takes time.", "Campus events are great for meeting people!"},
            {"I love Pomodoro! Also try active recall instead of just re-reading.", "Study groups help me stay accountable and understand better.", "Teaching someone else the material really solidifies it for me."},
            {"This is so helpful! I didn't know about these resources.", "Thanks for spreading awareness about mental health support!", "Bookmarking this. Everyone should know about these services."}
        };

        Random random = new Random();
        int totalComments = 0;
        
        for (int i = 0; i < posts.length; i++) {
            ForumPost post = new ForumPost();
            post.setTitle(posts[i][0]);
            post.setContent(posts[i][1]);
            post.setCategory(posts[i][2]);
            post.setUser(students.get(i % students.size()));
            post.setAnonymous(random.nextBoolean());
            post.setStatus("APPROVED");
            post.setLikeCount(random.nextInt(20) + 1);
            post.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(7)));
            post.setUpdatedAt(LocalDateTime.now());
            
            ForumPost savedPost = forumPostRepository.save(post);
            
            // Create 2-5 comments for each post
            int numComments = random.nextInt(4) + 2; // 2 to 5 comments
            for (int j = 0; j < numComments && j < commentTexts[i].length; j++) {
                ForumComment comment = new ForumComment();
                comment.setPost(savedPost);
                comment.setContent(commentTexts[i][j]);
                // Use different students for comments
                comment.setUser(students.get((i + j + 1) % students.size()));
                comment.setAnonymous(random.nextBoolean());
                comment.setCreatedAt(savedPost.getCreatedAt().plusHours(random.nextInt(24) + 1));
                
                forumCommentRepository.save(comment);
                totalComments++;
            }
        }

        System.out.println(LOG_PREFIX + " ✓ Created 10 sample forum posts with various categories.");
        System.out.println(LOG_PREFIX + " ✓ Created " + totalComments + " comments on forum posts.");
    }

    private void initializeForumLikes(List<User> students) {
        System.out.println(LOG_PREFIX + " Initializing forum post likes...");

        // Check if likes already exist to avoid duplicates
        if (forumPostLikeRepository.count() > 0) {
            System.out.println(LOG_PREFIX + " Forum likes already exist; skipping.");
            return;
        }

        List<ForumPost> allPosts = forumPostRepository.findAll();
        if (allPosts.isEmpty()) {
            System.out.println(LOG_PREFIX + " No forum posts available for liking.");
            return;
        }

        Random random = new Random();
        int likeCount = 0;

        // For each post, 2-5 random students like it
        for (ForumPost post : allPosts) {
            int numLikes = random.nextInt(4) + 2; // 2 to 5 likes per post
            
            // Shuffle students to get random ones
            List<User> shuffledStudents = new ArrayList<>(students);
            for (int i = 0; i < numLikes && i < shuffledStudents.size(); i++) {
                User studentWhoLikes = shuffledStudents.get(i);
                
                // Only create like if it doesn't already exist
                if (!forumPostLikeRepository.existsByPostAndUser(post, studentWhoLikes)) {
                    ForumPostLike like = new ForumPostLike(post, studentWhoLikes);
                    forumPostLikeRepository.save(like);
                    likeCount++;
                }
            }
        }

        System.out.println(LOG_PREFIX + " ✓ Created " + likeCount + " sample likes on forum posts.");
    }

    private void initializeForumFlags(List<User> students, User counselor, User counselor2) {
        System.out.println(LOG_PREFIX + " Initializing forum post flags...");

        // Check if flags already exist to avoid duplicates
        if (forumPostFlagRepository.count() > 0) {
            System.out.println(LOG_PREFIX + " Forum flags already exist; skipping.");
            return;
        }

        List<ForumPost> allPosts = forumPostRepository.findAll();
        if (allPosts.isEmpty()) {
            System.out.println(LOG_PREFIX + " No forum posts available for flagging.");
            return;
        }

        Random random = new Random();
        int flagCount = 0;

        // Sample flag reasons
        String[] flagReasons = {
            "Contains inappropriate language",
            "Potentially harmful content",
            "Spam or promotional content",
            "Off-topic discussion",
            "Violates forum guidelines",
            "Potentially distressing content",
            "Requires counselor review",
            "May need moderation"
        };

        // Flag approximately 3-4 posts with 1-3 flags each
        List<ForumPost> postsToFlag = new ArrayList<>();
        for (int i = 0; i < Math.min(4, allPosts.size()); i++) {
            postsToFlag.add(allPosts.get(i));
        }

        // For each selected post, add 1-3 flags from counselors
        for (ForumPost post : postsToFlag) {
            int numFlags = random.nextInt(3) + 1; // 1 to 3 flags per post

            for (int i = 0; i < numFlags; i++) {
                User flagger = i == 0 && counselor != null ? counselor : (counselor2 != null ? counselor2 : counselor);
                if (flagger != null && !forumPostFlagRepository.existsByPostAndUser(post, flagger)) {
                    String reason = flagReasons[random.nextInt(flagReasons.length)];
                    ForumPostFlag flag = new ForumPostFlag(post, flagger, reason);
                    forumPostFlagRepository.save(flag);
                    flagCount++;

                    // Update post's flagCount
                    long newFlagCount = forumPostFlagRepository.countByPost(post);
                    post.setFlagCount((int) newFlagCount);
                }
            }

            // Save the updated post with new flagCount
            if (post.getFlagCount() > 0) {
                forumPostRepository.save(post);
            }
        }

        System.out.println(LOG_PREFIX + " ✓ Created " + flagCount + " sample flags on forum posts.");
    }
}
package com.example.MentalMind.config;

import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.User;
import com.example.MentalMind.model.Feedback;
import com.example.MentalMind.model.CounselorResponse;
import com.example.MentalMind.model.Appointment;
import com.example.MentalMind.model.SelfAssessmentResult;
import com.example.MentalMind.model.CounselorSettings;
import com.example.MentalMind.repository.MoodEntryRepository;
import com.example.MentalMind.repository.UserRepository;
import com.example.MentalMind.repository.FeedbackRepository;
import com.example.MentalMind.repository.CounselorResponseRepository;
import com.example.MentalMind.repository.AppointmentRepository;
import com.example.MentalMind.repository.SelfAssessmentRepository;
import com.example.MentalMind.repository.CounselorSettingsRepository;
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

        System.out.println(LOG_PREFIX + " ✓ Total: 5 resources created.");
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
}

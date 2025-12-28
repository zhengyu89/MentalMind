package com.example.MentalMind.controller;

import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.Appointment;
import com.example.MentalMind.model.User;
import com.example.MentalMind.service.MoodService;
import com.example.MentalMind.service.SelfAssessmentService;
import com.example.MentalMind.service.AppointmentService;
import com.example.MentalMind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private MoodService moodService;

    @Autowired
    private SelfAssessmentService selfAssessmentService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId != null) {
            // Get mood entries as a map keyed by date (yyyy-MM-dd)
            java.util.Map<String, MoodEntry> moodMap = moodService.getWeeklyMoodsMap(userId);
            model.addAttribute("moodMap", moodMap);

            // Get mood statistics
            java.util.Map<String, Object> moodStats = moodService.getMoodStatistics(userId);
            model.addAttribute("moodStats", moodStats);

            // Generate last 7 days info for the template
            java.util.List<java.util.Map<String, String>> weekDays = new java.util.ArrayList<>();
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter dayFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE");

            for (int i = 6; i >= 0; i--) {
                java.time.LocalDate date = today.minusDays(i);
                java.util.Map<String, String> dayInfo = new java.util.HashMap<>();
                dayInfo.put("date", date.toString());
                dayInfo.put("dayName", i == 0 ? "Today" : date.format(dayFormatter));
                weekDays.add(dayInfo);
            }
            model.addAttribute("weekDays", weekDays);
        }

        return "student/dashboard";
    }

    @GetMapping("/learning")
    public String learning() {
        return "student/learning";
    }

    @GetMapping("/mood-tracker")
    public String moodTracker(@RequestParam(required = false, defaultValue = "week") String view,
            Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        // Pass view mode to template
        model.addAttribute("viewMode", view);

        // Pass current date info for calendar
        java.time.LocalDate today = java.time.LocalDate.now();
        model.addAttribute("currentYear", today.getYear());
        model.addAttribute("currentMonth", today.getMonthValue());
        model.addAttribute("currentMonthName", today.getMonth().toString());
        model.addAttribute("todayDate", today.toString());

        if (userId != null) {
            // Get mood statistics
            Map<String, Object> stats = moodService.getMoodStatistics(userId);
            model.addAttribute("moodStats", stats);

            // Get moods based on view mode
            if ("month".equals(view)) {
                Map<com.example.MentalMind.model.MoodEntry, String> monthlyMoods = new java.util.LinkedHashMap<>();
                moodService.getMonthlyMoodsMap(userId).forEach((date, entry) -> monthlyMoods.put(entry, date));
                model.addAttribute("moodEntriesMap", moodService.getMonthlyMoodsMap(userId));
            } else {
                model.addAttribute("moodEntriesMap", moodService.getWeeklyMoodsMap(userId));
            }

            // Check if today's mood is already logged
            Optional<MoodEntry> todaysMood = moodService.getTodaysMood(userId);
            model.addAttribute("todaysMood", todaysMood.orElse(null));
        }
        return "student/mood-tracker";
    }

    @PostMapping("/mood-tracker/log")
    public String logMood(@RequestParam Integer moodScore,
            @RequestParam(required = false) String notes,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/student/mood-tracker?error=notAuthenticated";
        }

        if (moodScore < 1 || moodScore > 5) {
            return "redirect:/student/mood-tracker?error=invalidScore";
        }

        try {
            moodService.logMood(userId, moodScore, notes);
            return "redirect:/student/mood-tracker?success=logged";
        } catch (Exception e) {
            return "redirect:/student/mood-tracker?error=failed";
        }
    }

    @PostMapping("/self-assessment/submit")
    public String submitSelfAssessment(@RequestParam Integer score, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/student/mood-tracker?error=notAuthenticated";
        }

        // Check if already completed today
        if (selfAssessmentService.hasCompletedToday(userId)) {
            return "redirect:/student/mood-tracker?error=alreadyCompleted";
        }

        if (score < 0 || score > 40) {
            return "redirect:/student/mood-tracker?error=invalidScore";
        }

        try {
            selfAssessmentService.saveResult(userId, score);
            return "redirect:/student/mood-tracker?success=assessment";
        } catch (Exception e) {
            return "redirect:/student/mood-tracker?error=assessmentFailed";
        }
    }

    @GetMapping("/resources")
    public String resources() {
        return "student/resources";
    }

    @GetMapping("/forum")
    public String forum() {
        return "student/forum";
    }

    @PostMapping("/forum/post")
    public String createForumPost(@RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "false") String anonymous,
            HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || title == null || title.isEmpty() ||
                content == null || content.isEmpty()) {
            return "redirect:/student/forum?error=invalid";
        }
        session.setAttribute("lastPost", title);
        session.setAttribute("postCategory", category != null ? category : "General");
        session.setAttribute("postAnonymous", anonymous);
        return "redirect:/student/forum?success=posted";
    }

    @GetMapping("/appointments")
    public String appointments(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User student = userOpt.get();

                // Get upcoming and past appointments
                List<Appointment> upcomingAppointments = appointmentService.getStudentUpcomingAppointments(student);
                List<Appointment> pastAppointments = appointmentService.getStudentPastAppointments(student);

                model.addAttribute("upcomingAppointments", upcomingAppointments);
                model.addAttribute("pastAppointments", pastAppointments);
                model.addAttribute("studentName", student.getFullName() != null ? student.getFullName() : "Student");
            }
        }

        return "student/appointments";
    }

    @PostMapping("/appointments/request")
    public String requestAppointment(@RequestParam String counselorId,
            @RequestParam String preferredDate,
            @RequestParam String preferredTime,
            @RequestParam(required = false) String reason,
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null || counselorId == null || counselorId.isEmpty() ||
                preferredDate == null || preferredDate.isEmpty() || preferredTime == null || preferredTime.isEmpty()) {
            return "redirect:/student/appointments?error=invalid";
        }

        try {
            // Get student and counselor users
            Optional<User> studentOpt = userRepository.findById(userId);
            Optional<User> counselorOpt = userRepository.findById(Long.parseLong(counselorId));

            if (studentOpt.isPresent() && counselorOpt.isPresent()) {
                User student = studentOpt.get();
                User counselor = counselorOpt.get();

                // Parse date and time
                LocalDate date = LocalDate.parse(preferredDate);
                LocalDateTime appointmentDateTime = date.atTime(
                    Integer.parseInt(preferredTime.split(":")[0]),
                    Integer.parseInt(preferredTime.split(":")[1])
                );

                // Create appointment
                Appointment appointment = appointmentService.createAppointment(
                    student, counselor, appointmentDateTime, reason != null ? reason : ""
                );

                session.setAttribute("lastAppointmentRequest", counselorId);
                session.setAttribute("preferredDate", preferredDate);
                session.setAttribute("preferredTime", preferredTime);
                return "redirect:/student/appointments?success=requested";
            }

            return "redirect:/student/appointments?error=invalid";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/student/appointments?error=failed";
        }
    }

    @GetMapping("/emergency")
    public String emergency() {
        return "student/emergency";
    }

    @GetMapping("/recommendations")
    public String recommendations() {
        return "student/recommendations";
    }

    @GetMapping("/feedback")
    public String feedback() {
        return "student/feedback";
    }

    @PostMapping("/feedback/submit")
    public String submitFeedback(@RequestParam String feedbackType,
            @RequestParam String message,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) String email,
            HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || message == null || message.isEmpty() ||
                feedbackType == null || feedbackType.isEmpty()) {
            return "redirect:/student/feedback?error=invalid";
        }
        session.setAttribute("lastFeedbackType", feedbackType);
        session.setAttribute("feedbackRating", rating != null ? rating : "");
        session.setAttribute("feedbackContact", email != null ? email : "");
        return "redirect:/student/feedback?success=submitted";
    }
}

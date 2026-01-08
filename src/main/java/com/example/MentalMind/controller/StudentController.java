package com.example.MentalMind.controller;

import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.User;

import com.example.MentalMind.service.MoodService;
import com.example.MentalMind.service.ResourceService;
import com.example.MentalMind.service.SelfAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

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
    private ResourceService resourceService;

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

    @GetMapping("/api/resources")
    @ResponseBody
    public List<com.example.MentalMind.model.Resource> getResources(@RequestParam(required = false) String type,
                                                                   @RequestParam(required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return resourceService.searchResources(search.trim());
        } else if (type != null && !type.equals("all")) {
            return resourceService.getResourcesByType(type);
        } else {
            return resourceService.getAllResources();
        }
    }

    @GetMapping("/api/resources/{id}")
    @ResponseBody
    public Optional<com.example.MentalMind.model.Resource> getResource(@PathVariable Long id) {
        Optional<com.example.MentalMind.model.Resource> opt = resourceService.getResourceById(id);
        opt.ifPresent(r -> {
            if (r.getContent() != null) {
                r.setContent(r.getContent().replace("\n", "<br/>") );
            }
        });
        return opt;
    }

    @GetMapping("/api/bookmarks")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> getBookmarks(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            // return session-stored bookmarks for unauthenticated users (if any)
            java.util.List<java.util.Map<String, Object>> temp = (java.util.List<java.util.Map<String, Object>>) session.getAttribute("tempBookmarks");
            if (temp == null) return java.util.Collections.emptyList();
            return temp;
        }
        // Convert persistent bookmarks to a simple map representation for the client
        java.util.List<com.example.MentalMind.model.UserResourceBookmark> bookmarks = resourceService.getUserBookmarks(userId);
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (com.example.MentalMind.model.UserResourceBookmark b : bookmarks) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("resourceId", b.getResource().getId());
            m.put("title", b.getResource().getTitle());
            m.put("type", b.getResource().getType());
            m.put("bookmarkType", b.getBookmarkType());
            out.add(m);
        }
        return out;
    }

    @PostMapping("/api/bookmarks")
    @ResponseBody
    public Map<String, String> addBookmark(@RequestParam Long resourceId,
                                          @RequestParam String bookmarkType,
                                          HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            // store bookmark in session for unauthenticated users
            java.util.Optional<com.example.MentalMind.model.Resource> maybe = resourceService.getResourceById(resourceId);
            if (maybe.isEmpty()) {
                return Map.of("status", "error", "message", "Resource not found");
            }
            com.example.MentalMind.model.Resource res = maybe.get();

            java.util.List<java.util.Map<String, Object>> temp = (java.util.List<java.util.Map<String, Object>>) session.getAttribute("tempBookmarks");
            if (temp == null) {
                temp = new java.util.ArrayList<>();
            }
            // prevent duplicates
            boolean exists = temp.stream().anyMatch(m -> ((Number) m.getOrDefault("resourceId", -1)).longValue() == resourceId && bookmarkType.equals(m.getOrDefault("bookmarkType", "")));
            if (exists) {
                session.setAttribute("tempBookmarks", temp);
                return Map.of("status", "error", "message", "Bookmark already exists");
            }

            java.util.Map<String, Object> entry = new java.util.HashMap<>();
            entry.put("resourceId", resourceId);
            entry.put("title", res.getTitle());
            entry.put("type", res.getType());
            entry.put("bookmarkType", bookmarkType);
            temp.add(entry);
            session.setAttribute("tempBookmarks", temp);
            return Map.of("status", "success", "message", "Bookmark added (session)");
        }

        try {
            resourceService.addBookmark(userId, resourceId, bookmarkType);
            return Map.of("status", "success", "message", "Bookmark added successfully");
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @DeleteMapping("/api/bookmarks")
    @ResponseBody
    public Map<String, String> removeBookmark(@RequestParam Long resourceId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            java.util.List<java.util.Map<String, Object>> temp = (java.util.List<java.util.Map<String, Object>>) session.getAttribute("tempBookmarks");
            if (temp == null) return Map.of("status", "error", "message", "No bookmarks in session");
            boolean removed = temp.removeIf(m -> ((Number) m.getOrDefault("resourceId", -1)).longValue() == resourceId);
            session.setAttribute("tempBookmarks", temp);
            if (removed) return Map.of("status", "success", "message", "Bookmark removed (session)");
            else return Map.of("status", "error", "message", "Bookmark not found");
        }

        try {
            resourceService.removeBookmark(userId, resourceId);
            return Map.of("status", "success", "message", "Bookmark removed successfully");
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
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
    public String appointments() {
        return "student/appointments";
    }

    @PostMapping("/appointments/request")
    public String requestAppointment(@RequestParam String counselorId,
            @RequestParam String preferredDate,
            @RequestParam String preferredTime,
            @RequestParam(required = false) String reason,
            HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || counselorId == null || counselorId.isEmpty() ||
                preferredDate == null || preferredDate.isEmpty() || preferredTime == null || preferredTime.isEmpty()) {
            return "redirect:/student/appointments?error=invalid";
        }
        session.setAttribute("lastAppointmentRequest", counselorId);
        session.setAttribute("preferredDate", preferredDate);
        session.setAttribute("preferredTime", preferredTime);
        session.setAttribute("appointmentReason", reason != null ? reason : "");
        return "redirect:/student/appointments?success=requested";
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

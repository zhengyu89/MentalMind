package com.example.MentalMind.controller;

import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.Feedback;
import com.example.MentalMind.model.Appointment;
import com.example.MentalMind.model.User;
import com.example.MentalMind.model.StudentSettings;
import com.example.MentalMind.model.LearningModule;
import com.example.MentalMind.model.LearningMaterial;
import com.example.MentalMind.model.ForumPost;
import com.example.MentalMind.model.ForumComment;
import com.example.MentalMind.service.MoodService;
import com.example.MentalMind.service.ResourceService;
import com.example.MentalMind.service.SelfAssessmentService;
import com.example.MentalMind.service.FeedbackService;
import com.example.MentalMind.service.AppointmentService;
import com.example.MentalMind.service.StudentSettingsService;
import com.example.MentalMind.service.LearningProgressService;
import com.example.MentalMind.service.ForumService;
import com.example.MentalMind.repository.UserRepository;
import com.example.MentalMind.repository.LearningModuleRepository;
import com.example.MentalMind.repository.LearningMaterialRepository;
import com.example.MentalMind.repository.ForumPostRepository;
import com.example.MentalMind.repository.ForumPostLikeRepository;
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
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

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
    private ResourceService resourceService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentSettingsService studentSettingsService;

    @Autowired
    private LearningModuleRepository learningModuleRepository;

    @Autowired
    private LearningMaterialRepository learningMaterialRepository;

    @Autowired
    private LearningProgressService learningProgressService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private ForumPostLikeRepository forumPostLikeRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId != null) {
            // Get user and settings
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                model.addAttribute("userFullName", user.get().getFullName());
                StudentSettings settings = studentSettingsService.getOrCreateSettings(user.get());
                model.addAttribute("userPhotoUrl", settings.getProfilePhotoUrl());
            }

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
    public String learning(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId != null) {
            // Get user info
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                model.addAttribute("userFullName", user.get().getFullName());
                StudentSettings settings = studentSettingsService.getOrCreateSettings(user.get());
                model.addAttribute("userPhotoUrl", settings.getProfilePhotoUrl());
            }
            
            // Get all active learning modules
            List<LearningModule> modules = learningModuleRepository.findByIsActiveTrue();
            model.addAttribute("modules", modules);
            
            // Get progress for all modules
            Map<Long, Integer> progressMap = learningProgressService.getAllModulesProgress(userId, modules);
            model.addAttribute("progressMap", progressMap);
        }
        
        return "student/learning";
    }

    @GetMapping("/learning/module/{moduleId}")
    public String viewModule(@PathVariable Long moduleId, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        // Get user info
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            model.addAttribute("userFullName", user.get().getFullName());
            StudentSettings settings = studentSettingsService.getOrCreateSettings(user.get());
            model.addAttribute("userPhotoUrl", settings.getProfilePhotoUrl());
        }
        
        // Get the module
        Optional<LearningModule> moduleOpt = learningModuleRepository.findById(moduleId);
        if (moduleOpt.isEmpty() || !moduleOpt.get().getIsActive()) {
            return "redirect:/student/learning";
        }
        
        LearningModule module = moduleOpt.get();
        model.addAttribute("module", module);
        
        // Filter only active materials
        List<LearningMaterial> activeMaterials = module.getMaterials().stream()
            .filter(m -> m.getIsActive())
            .toList();
        model.addAttribute("materials", activeMaterials);
        
        // Get completed material IDs for this student and module
        java.util.Set<Long> completedMaterialIds = learningProgressService.getCompletedMaterialIdsForModule(userId, moduleId);
        model.addAttribute("completedMaterialIds", completedMaterialIds);
        
        // Calculate progress percentage
        int progressPercentage = learningProgressService.getModuleProgressPercentage(userId, module);
        model.addAttribute("progressPercentage", progressPercentage);
        model.addAttribute("completedCount", completedMaterialIds.size());
        model.addAttribute("totalCount", activeMaterials.size());
        
        return "student/module-details";
    }

    @PostMapping("/learning/material/{materialId}/complete")
    @ResponseBody
    public ResponseEntity<?> markMaterialComplete(@PathVariable Long materialId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            learningProgressService.markAsCompleted(userId, materialId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Material marked as completed"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/learning/material/{materialId}/incomplete")
    @ResponseBody
    public ResponseEntity<?> markMaterialIncomplete(@PathVariable Long materialId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            learningProgressService.markAsIncomplete(userId, materialId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Material marked as incomplete"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
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
                r.setContent(r.getContent().replace("\n", "<br/>"));
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
            java.util.List<java.util.Map<String, Object>> temp = (java.util.List<java.util.Map<String, Object>>) session
                    .getAttribute("tempBookmarks");
            if (temp == null)
                return java.util.Collections.emptyList();
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
            java.util.Optional<com.example.MentalMind.model.Resource> maybe = resourceService
                    .getResourceById(resourceId);
            if (maybe.isEmpty()) {
                return Map.of("status", "error", "message", "Resource not found");
            }
            com.example.MentalMind.model.Resource res = maybe.get();

            java.util.List<java.util.Map<String, Object>> temp = (java.util.List<java.util.Map<String, Object>>) session
                    .getAttribute("tempBookmarks");
            if (temp == null) {
                temp = new java.util.ArrayList<>();
            }
            // prevent duplicates
            boolean exists = temp.stream()
                    .anyMatch(m -> ((Number) m.getOrDefault("resourceId", -1)).longValue() == resourceId
                            && bookmarkType.equals(m.getOrDefault("bookmarkType", "")));
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
            if (removed)
                return Map.of("status", "success", "message", "Bookmark removed (session)");
            else
                return Map.of("status", "error", "message", "Bookmark not found");
        }

        try {
            resourceService.removeBookmark(userId, resourceId);
            return Map.of("status", "success", "message", "Bookmark removed successfully");
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @GetMapping("/forum")
    public String forum(Model model, HttpSession session,
                       @RequestParam(required = false) String category,
                       @RequestParam(defaultValue = "time") String sort) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            model.addAttribute("userFullName", user.get().getFullName());
            StudentSettings settings = studentSettingsService.getOrCreateSettings(user.get());
            model.addAttribute("userPhotoUrl", settings.getProfilePhotoUrl());
        }

        // Get posts by category, user's own posts, or all approved posts
        List<ForumPost> posts;
        if ("myposts".equalsIgnoreCase(category) && user.isPresent()) {
            // Show only user's own approved posts (pending posts are shown separately above)
            posts = forumPostRepository.findByUserAndStatus(user.get(), "APPROVED");
        } else if (category != null && !category.equalsIgnoreCase("all")) {
            posts = forumService.getPostsByCategory(category);
        } else {
            posts = forumService.getApprovedPosts();
        }

        // Sort posts based on sort parameter
        if ("likes".equalsIgnoreCase(sort)) {
            posts.sort((p1, p2) -> Integer.compare(p2.getLikeCount(), p1.getLikeCount()));
        } else {
            // Default: sort by time (newest first)
            posts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
        }

        // Build a set of liked post IDs for current user
        if (user.isPresent()) {
            var likes = forumPostLikeRepository.findByUser(user.get());
            java.util.Set<Long> likedPostIds = likes.stream()
                .map(like -> like.getPost().getId())
                .collect(java.util.stream.Collectors.toSet());
            model.addAttribute("likedPostIds", likedPostIds);

            // Build a map of user's flagged posts
            java.util.Map<Long, Boolean> userFlaggedPosts = new java.util.HashMap<>();
            for (ForumPost post : posts) {
                userFlaggedPosts.put(post.getId(), forumService.hasUserFlaggedPost(post, user.get()));
            }
            model.addAttribute("userFlaggedPosts", userFlaggedPosts);

            // Add current user ID and owned posts for post actions
            model.addAttribute("currentUserId", userId);
            java.util.Set<Long> userOwnedPostIds = new java.util.HashSet<>();
            for (ForumPost post : posts) {
                if (post.getUser() != null && post.getUser().getId() != null && 
                    post.getUser().getId().equals(user.get().getId())) {
                    userOwnedPostIds.add(post.getId());
                }
            }
            model.addAttribute("userOwnedPostIds", userOwnedPostIds);

            // Get user's own pending posts
            List<ForumPost> pendingPosts = forumPostRepository.findByUserAndStatus(user.get(), "PENDING");
            pendingPosts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
            model.addAttribute("pendingPosts", pendingPosts);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("selectedCategory", category != null ? category : "all");
        model.addAttribute("sortBy", sort);

        return "student/forum";
    }

    @GetMapping("/forum/{postId}")
    public String forumPostDetail(Model model, HttpSession session, @PathVariable Long postId) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            model.addAttribute("userFullName", user.get().getFullName());
            StudentSettings settings = studentSettingsService.getOrCreateSettings(user.get());
            model.addAttribute("userPhotoUrl", settings.getProfilePhotoUrl());
        }

        // Get the specific post
        Optional<ForumPost> post = forumPostRepository.findById(postId);
        if (post.isEmpty() || !"APPROVED".equals(post.get().getStatus())) {
            return "redirect:/student/forum?error=post_not_found";
        }

        // Sort comments by newest first
        post.get().getComments().sort((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()));

        model.addAttribute("post", post.get());

        // Liked flag for current user on detail page
        if (user.isPresent()) {
            boolean liked = forumPostLikeRepository.existsByPostAndUser(post.get(), user.get());
            model.addAttribute("likedByCurrentUser", liked);

            // Check if user has already flagged this post
            boolean alreadyFlagged = forumService.hasUserFlaggedPost(post.get(), user.get());
            model.addAttribute("alreadyFlagged", alreadyFlagged);

            // Check if this is user's own post
            boolean isOwnPost = post.get().getUser() != null && 
                                post.get().getUser().getId() != null &&
                                post.get().getUser().getId().equals(user.get().getId());
            model.addAttribute("isOwnPost", isOwnPost);
            model.addAttribute("currentUserId", userId);
        }
        return "student/forum-detail";
    }

    @PostMapping("/forum/post")
    public String createForumPost(@RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "false") boolean anonymous,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null || title == null || title.trim().isEmpty() ||
                content == null || content.trim().isEmpty()) {
            return "redirect:/student/forum?error=invalid";
        }

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return "redirect:/student/forum?error=user_not_found";
        }

        // Default category to "general" if not provided
        String postCategory = (category != null && !category.trim().isEmpty()) ? category.toLowerCase() : "general";

        forumService.createPost(user.get(), title.trim(), content.trim(), postCategory, anonymous);
        return "redirect:/student/forum?success=posted";
    }

    @PostMapping("/forum/like/{postId}")
    @ResponseBody
    public Map<String, Object> likePost(@PathVariable Long postId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Map.of("status", "error", "message", "Not authenticated");
        }

        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                return Map.of("status", "error", "message", "User not found");
            }
            var result = forumService.toggleLike(postId, user.get());
            return Map.of(
                "status", "success",
                "likeCount", result.getLikeCount(),
                "liked", result.isLiked()
            );
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @PostMapping("/forum/comment/{postId}")
    @ResponseBody
    public Map<String, Object> addComment(@PathVariable Long postId,
                                          @RequestParam String content,
                                          @RequestParam(defaultValue = "true") boolean anonymous,
                                          HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Map.of("status", "error", "message", "Not authenticated");
        }

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return Map.of("status", "error", "message", "User not found");
        }

        try {
            ForumComment comment = forumService.addComment(postId, user.get(), content.trim(), anonymous);
            return Map.of(
                    "status", "success",
                    "comment", Map.of(
                            "id", comment.getId(),
                            "content", comment.getContent(),
                            "authorName", comment.getAuthorDisplayName(),
                            "timeAgo", comment.getTimeAgo()
                    )
            );
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @PostMapping("/forum/flag/{postId}")
    @ResponseBody
    public Map<String, Object> flagPost(@PathVariable Long postId,
                                        @RequestParam String reason,
                                        HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Map.of("status", "error", "message", "Not authenticated");
        }

        try {
            User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            forumService.flagPost(postId, student, reason);
            return Map.of("status", "success", "message", "Post flagged for moderation");
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @PostMapping("/forum/moderate")
    @ResponseBody
    public Map<String, Object> moderatePost(@RequestParam Long postId,
                                            @RequestParam String action,
                                            @RequestParam(required = false) String moderationNote,
                                            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Map.of("status", "error", "message", "Not authenticated");
        }

        try {
            switch (action.toLowerCase()) {
                case "flag":
                    User student = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Student not found"));
                    forumService.flagPost(postId, student, moderationNote);
                    return Map.of("status", "success", "message", "Post flagged for moderation");
                default:
                    return Map.of("status", "error", "message", "Invalid action");
            }
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @PostMapping("/forum/delete")
    @ResponseBody
    public Map<String, Object> deletePost(@RequestParam Long postId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Map.of("status", "error", "message", "Not authenticated");
        }

        try {
            Optional<User> user = userRepository.findById(userId);
            Optional<ForumPost> post = forumPostRepository.findById(postId);
            
            if (user.isEmpty()) {
                return Map.of("status", "error", "message", "User not found");
            }
            
            if (post.isEmpty()) {
                return Map.of("status", "error", "message", "Post not found");
            }
            
            // Check if user is the owner of the post
            if (!post.get().getUser().getId().equals(user.get().getId())) {
                return Map.of("status", "error", "message", "You can only delete your own posts");
            }
            
            forumPostRepository.delete(post.get());
            return Map.of("status", "success", "message", "Post deleted successfully");
        } catch (Exception e) {
            return Map.of("status", "error", "message", "Failed to delete post: " + e.getMessage());
        }
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
                
                // Get all counselors
                List<User> counselors = userRepository.findByRole("counselor");

                model.addAttribute("upcomingAppointments", upcomingAppointments);
                model.addAttribute("pastAppointments", pastAppointments);
                model.addAttribute("counselors", counselors);
                model.addAttribute("studentName", student.getFullName() != null ? student.getFullName() : "Student");
            }
        }

        return "student/appointments";
    }

    @GetMapping("/api/slots")
    @ResponseBody
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getAvailableSlots(@RequestParam Long counselorId,
                                                                                             @RequestParam String date) {
        try {
            Optional<User> counselorOpt = userRepository.findById(counselorId);
            if (counselorOpt.isEmpty()) {
                return new ResponseEntity<>(java.util.Collections.emptyList(), HttpStatus.NOT_FOUND);
            }
            java.time.LocalDate d = java.time.LocalDate.parse(date);
            java.util.List<java.util.Map<String, Object>> slots = appointmentService.getAvailableSlots(counselorOpt.get(), d);
            return new ResponseEntity<>(slots, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(java.util.Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/check-conflict")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> checkConflict(@RequestParam String date,
                                                                        @RequestParam String time,
                                                                        @RequestParam(required = false) Long appointmentId,
                                                                        HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new ResponseEntity<>(java.util.Map.of("conflict", false, "message", "Not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        try {
            Optional<User> studentOpt = userRepository.findById(userId);
            if (studentOpt.isEmpty()) {
                return new ResponseEntity<>(java.util.Map.of("conflict", false, "message", "Student not found"), HttpStatus.NOT_FOUND);
            }

            LocalDate d = LocalDate.parse(date);
            LocalDateTime dateTime = d.atTime(
                    Integer.parseInt(time.split(":" )[0]),
                    Integer.parseInt(time.split(":" )[1]));

            boolean conflict = appointmentService.hasStudentConflict(studentOpt.get(), dateTime, appointmentId);
            if (conflict) {
                return new ResponseEntity<>(java.util.Map.of(
                        "conflict", true,
                        "message", "You already have a pending or approved appointment at this time."), HttpStatus.OK);
            }

            return new ResponseEntity<>(java.util.Map.of("conflict", false), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(java.util.Map.of("conflict", false, "message", "Error checking conflict"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
                        Integer.parseInt(preferredTime.split(":")[1]));

                // Prevent double-booking across counselors at the same time
                if (appointmentService.hasStudentConflict(student, appointmentDateTime, null)) {
                    return "redirect:/student/appointments?error=conflict";
                }

                // Create appointment
                Appointment appointment = appointmentService.createAppointment(
                        student, counselor, appointmentDateTime, reason != null ? reason : "");

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

    @PostMapping("/appointments/reschedule")
    public String rescheduleAppointment(@RequestParam Long appointmentId,
            @RequestParam String preferredDate,
            @RequestParam String preferredTime,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null || appointmentId == null || preferredDate == null || preferredDate.isEmpty() 
                || preferredTime == null || preferredTime.isEmpty()) {
            return "redirect:/student/appointments?error=invalid";
        }

        try {
            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentById(appointmentId);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                
                // Verify the appointment belongs to the current student
                if (!appointment.getStudent().getId().equals(userId)) {
                    return "redirect:/student/appointments?error=unauthorized";
                }
                
                // Parse new date and time
                LocalDate date = LocalDate.parse(preferredDate);
                LocalDateTime newAppointmentDateTime = date.atTime(
                        Integer.parseInt(preferredTime.split(":")[0]),
                        Integer.parseInt(preferredTime.split(":")[1]));

                // Prevent double-booking across counselors at the same time (exclude this appointment id)
                if (appointmentService.hasStudentConflict(appointment.getStudent(), newAppointmentDateTime, appointment.getId())) {
                    return "redirect:/student/appointments?error=conflict";
                }
                
                // Update appointment
                appointment.setAppointmentDateTime(newAppointmentDateTime);
                appointment.setUpdatedAt(LocalDateTime.now());
                appointmentService.updateAppointment(appointment);
                
                return "redirect:/student/appointments?success=rescheduled";
            }
            
            return "redirect:/student/appointments?error=notfound";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/student/appointments?error=failed";
        }
    }

    @PostMapping("/appointments/delete")
    public String deleteAppointment(@RequestParam Long appointmentId,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null || appointmentId == null) {
            return "redirect:/student/appointments?error=invalid";
        }

        try {
            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentById(appointmentId);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                
                // Verify the appointment belongs to the current student
                if (!appointment.getStudent().getId().equals(userId)) {
                    return "redirect:/student/appointments?error=unauthorized";
                }
                
                // Delete appointment
                appointmentService.deleteAppointment(appointmentId);
                
                return "redirect:/student/appointments?success=deleted";
            }
            
            return "redirect:/student/appointments?error=notfound";
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
    public String feedback(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId != null) {
            java.util.List<Feedback> recentFeedback = feedbackService.getRecentUserFeedback(userId);
            model.addAttribute("recentFeedback", recentFeedback);
        }

        return "student/feedback";
    }

    @PostMapping("/feedback/submit")
    @ResponseBody
    public ResponseEntity<?> submitFeedback(
            @RequestParam String type,
            @RequestParam String subject,
            @RequestParam String details,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("success", false, "message", "Not authenticated"));
        }

        if (type == null || type.isEmpty() || subject == null || subject.isEmpty() ||
                details == null || details.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("success", false, "message", "All fields are required"));
        }

        // Validate feedback type
        if (!type.matches("feedback|bug|suggestion")) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("success", false, "message", "Invalid feedback type"));
        }

        try {
            java.util.Optional<User> user = userRepository.findById(userId);

            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of("success", false, "message", "User not found"));
            }

            Feedback feedback = feedbackService.submitFeedback(user.get(), type, subject, details);

            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "message", "Feedback submitted successfully",
                    "feedbackId", feedback.getId(),
                    "createdAt", feedback.getCreatedAt().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("success", false, "message",
                            "Error submitting feedback: " + e.getMessage()));
        }
    }

    @GetMapping("/feedback/list")
    @ResponseBody
    public ResponseEntity<?> getUserFeedback(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("success", false, "message", "Not authenticated"));
        }

        try {
            java.util.List<Feedback> feedbackList = feedbackService.getUserFeedback(userId);
            java.util.List<java.util.Map<String, Object>> feedbackData = new java.util.ArrayList<>();

            for (Feedback f : feedbackList) {
                feedbackData.add(java.util.Map.of(
                        "id", f.getId(),
                        "type", f.getType(),
                        "subject", f.getSubject(),
                        "details", f.getDetails(),
                        "status", f.getStatus(),
                        "createdAt", f.getCreatedAt().toString()));
            }

            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "feedback", feedbackData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("success", false, "message", "Error fetching feedback"));
        }
    }

    // ==================== SETTINGS ENDPOINTS ====================

    @GetMapping("/settings")
    public String settings(HttpSession session, Model model) {
        if (session.getAttribute("isAuthenticated") == null || !"student".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("userId");
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User student = userOpt.get();
        StudentSettings settings = studentSettingsService.getOrCreateSettings(student);

        model.addAttribute("student", student);
        model.addAttribute("settings", settings);

        return "student/settings";
    }

    @PostMapping("/settings/profile")
    @ResponseBody
    public ResponseEntity<?> updateProfile(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String faculty,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String yearOfStudy,
            HttpSession session) {

        if (session.getAttribute("isAuthenticated") == null || !"student".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            Long userId = (Long) session.getAttribute("userId");
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "User not found"));
            }

            User student = userOpt.get();

            // Update user entity fields
            if (fullName != null && !fullName.isEmpty()) {
                student.setFullName(fullName);
            }
            if (email != null && !email.isEmpty()) {
                student.setEmail(email);
            }
            if (phone != null) {
                student.setPhoneNumber(phone);
            }
            student.setUpdatedAt(LocalDateTime.now());
            userRepository.save(student);

            // Update settings entity fields
            studentSettingsService.updateProfile(student, bio, faculty, course, yearOfStudy, null);

            return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error updating profile: " + e.getMessage()));
        }
    }

    @PostMapping("/settings/photo")
    @ResponseBody
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("photo") org.springframework.web.multipart.MultipartFile file,
            HttpSession session) {

        if (session.getAttribute("isAuthenticated") == null || !"student".equals(session.getAttribute("userRole"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No file selected"));
        }

        // Check file size (max 2MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "File size must be less than 2MB"));
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png")
                && !contentType.equals("image/gif"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Only JPG, PNG, and GIF images are allowed"));
        }

        try {
            Long userId = (Long) session.getAttribute("userId");
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "User not found"));
            }

            User student = userOpt.get();

            // Create uploads directory if it doesn't exist
            String uploadDir = "src/main/resources/static/uploads/profiles";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = "student_" + userId + "_" + System.currentTimeMillis() + extension;

            // Save the file
            java.nio.file.Path filePath = uploadPath.resolve(filename);
            java.nio.file.Files.copy(file.getInputStream(), filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Update the profile photo URL in the database
            String photoUrl = "/uploads/profiles/" + filename;
            studentSettingsService.updatePhoto(student, photoUrl);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Photo uploaded successfully",
                    "photoUrl", photoUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error uploading photo: " + e.getMessage()));
        }
    }
}

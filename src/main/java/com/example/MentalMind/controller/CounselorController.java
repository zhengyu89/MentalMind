package com.example.MentalMind.controller;

import com.example.MentalMind.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/counselor")
public class CounselorController {

    @Autowired
    private ResourceService resourceService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "counselor/dashboard";
    }

    @GetMapping("/appointments")
    public String appointments() {
        return "counselor/appointments";
    }

    @PostMapping("/appointments/approve")
    public String approveAppointment(@RequestParam String appointmentId, HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || appointmentId == null || appointmentId.isEmpty()) {
            return "redirect:/counselor/appointments?error=invalid";
        }
        session.setAttribute("lastAction", "approved");
        session.setAttribute("appointmentId", appointmentId);
        return "redirect:/counselor/appointments?success=approved";
    }

    @PostMapping("/appointments/reject")
    public String rejectAppointment(@RequestParam String appointmentId,
                                   @RequestParam(required = false) String reason,
                                   HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || appointmentId == null || appointmentId.isEmpty()) {
            return "redirect:/counselor/appointments?error=invalid";
        }
        session.setAttribute("lastAction", "rejected");
        session.setAttribute("appointmentId", appointmentId);
        session.setAttribute("rejectionReason", reason != null ? reason : "No reason provided");
        return "redirect:/counselor/appointments?success=rejected";
    }

    @GetMapping("/students")
    public String students() {
        return "counselor/students";
    }

    @GetMapping("/resources")
    public String resources() {
        return "counselor/resources";
    }

    @PostMapping("/resources/upload")
    public String uploadResource(@RequestParam String resourceTitle,
                               @RequestParam String resourceType,
                               @RequestParam(required = false) String resourceUrl,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false) String category,
                               HttpSession session) {
        // Title is always required. URL is required only for videos.
        if (resourceTitle == null || resourceTitle.isEmpty()) {
            return "redirect:/counselor/resources?error=invalid";
        }
        if ("video".equalsIgnoreCase(resourceType) && (resourceUrl == null || resourceUrl.isEmpty())) {
            return "redirect:/counselor/resources?error=invalid";
        }

        try {
            // Determine content type and generate content
            String content = generateResourceContent(resourceType, resourceUrl, resourceTitle, description);
            String coverImageUrl = extractCoverImageUrl(resourceType, resourceUrl);

            // Set default styling based on type
            String icon = getIconForType(resourceType);
            String gradientFrom = getGradientFromForType(resourceType);
            String gradientTo = getGradientToForType(resourceType);
            String badgeColor = getBadgeColorForType(resourceType);

            // Create the resource (include category)
            resourceService.createResource(
                resourceTitle,
                resourceType,
                description != null ? description : "",
                content,
                icon,
                gradientFrom,
                gradientTo,
                badgeColor,
                category,
                coverImageUrl
            );

            session.setAttribute("lastResource", resourceTitle);
            return "redirect:/counselor/resources?success=uploaded";
        } catch (Exception e) {
            session.setAttribute("uploadError", e.getMessage());
            return "redirect:/counselor/resources?error=upload_failed";
        }
    }

    @PostMapping("/resources/delete")
    public String deleteResource(@RequestParam Long resourceId, HttpSession session) {
        try {
            resourceService.deactivateResource(resourceId);
            return "redirect:/counselor/resources?success=deleted";
        } catch (Exception e) {
            return "redirect:/counselor/resources?error=delete_failed";
        }
    }

    @PostMapping("/resources/update")
    public String updateResource(@RequestParam Long resourceId,
                                 @RequestParam String resourceTitle,
                                 @RequestParam String resourceType,
                                 @RequestParam(required = false) String resourceUrl,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false) String category,
                                 HttpSession session) {
        try {
            // If updating a non-video resource, allow empty resourceUrl
            if ("video".equalsIgnoreCase(resourceType) && (resourceUrl == null || resourceUrl.isEmpty())) {
                return "redirect:/counselor/resources?error=invalid";
            }
            String content = generateResourceContent(resourceType, resourceUrl, resourceTitle, description);
            String coverImageUrl = extractCoverImageUrl(resourceType, resourceUrl);
            String icon = getIconForType(resourceType);
            String gradientFrom = getGradientFromForType(resourceType);
            String gradientTo = getGradientToForType(resourceType);
            String badgeColor = getBadgeColorForType(resourceType);

            resourceService.updateResource(resourceId, resourceTitle, resourceType, description != null ? description : "", content, icon, gradientFrom, gradientTo, badgeColor, category, coverImageUrl);
            return "redirect:/counselor/resources?success=updated";
        } catch (Exception e) {
            return "redirect:/counselor/resources?error=update_failed";
        }
    }

    @GetMapping("/forum")
    public String forum() {
        return "counselor/forum";
    }

    @PostMapping("/forum/moderate")
    public String moderatePost(@RequestParam String postId,
                             @RequestParam String action,
                             @RequestParam(required = false) String moderationNote,
                             HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null || postId == null || postId.isEmpty() ||
            action == null || (!action.equals("approve") && !action.equals("reject") && !action.equals("flag"))) {
            return "redirect:/counselor/forum?error=invalid";
        }
        session.setAttribute("lastModerationAction", action);
        session.setAttribute("moderatedPostId", postId);
        session.setAttribute("moderationNote", moderationNote != null ? moderationNote : "");
        return "redirect:/counselor/forum?success=moderated";
    }

    @GetMapping("/reports")
    public String reports() {
        return "counselor/reports";
    }

    @GetMapping("/settings")
    public String settings() {
        return "counselor/settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam(required = false) String fullName,
                               @RequestParam(required = false) String specialization,
                               @RequestParam(required = false) String availabilityHours,
                               @RequestParam(required = false) String bio,
                               @RequestParam(defaultValue = "false") String notificationsEnabled,
                               HttpSession session) {
        if (session.getAttribute("isAuthenticated") == null) {
            return "redirect:/counselor/settings?error=unauthorized";
        }
        session.setAttribute("counselorName", fullName != null ? fullName : "");
        session.setAttribute("specialization", specialization != null ? specialization : "");
        session.setAttribute("availabilityHours", availabilityHours != null ? availabilityHours : "");
        session.setAttribute("bio", bio != null ? bio : "");
        session.setAttribute("notificationsEnabled", notificationsEnabled);
        return "redirect:/counselor/settings?success=updated";
    }
    private String generateResourceContent(String type, String url, String title, String description) {
        String safeDesc = description == null ? "" : description.replace("\n", "<br/>");
        switch (type.toLowerCase()) {
            case "video":
                if (isYouTubeUrl(url)) {
                    return "<div class=\"aspect-video bg-slate-900 rounded-lg mb-6 overflow-hidden\"><iframe width=\"100%\" height=\"100%\" src=\"" + convertToEmbedUrl(url) + "\" title=\"" + title + "\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen></iframe></div><div class=\"prose dark:prose-invert max-w-none\"><p>" + safeDesc + "</p></div>";
                } else {
                    return "<div class=\"aspect-video bg-slate-900 rounded-lg mb-6 overflow-hidden\"><video width=\"100%\" height=\"100%\" controls><source src=\"" + url + "\" type=\"video/mp4\">Your browser does not support the video tag.</video></div><div class=\"prose dark:prose-invert max-w-none\"><p>" + safeDesc + "</p></div>";
                }
            case "article":
                return "<div class=\"prose dark:prose-invert max-w-none\"><p>Article content would be loaded from: " + url + "</p><p>" + safeDesc + "</p></div>";
            case "guide":
                return "<div class=\"prose dark:prose-invert max-w-none\"><h3>Guide: " + title + "</h3><p>Guide content would be loaded from: " + url + "</p><p>" + safeDesc + "</p></div>";
            default:
                return "<div class=\"prose dark:prose-invert max-w-none\"><p>Content: " + url + "</p><p>" + safeDesc + "</p></div>";
        }
    }

    private String extractCoverImageUrl(String type, String url) {
        if (type.equalsIgnoreCase("video") && isYouTubeUrl(url)) {
            String videoId = extractYouTubeVideoId(url);
            if (videoId != null) {
                return "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
            }
        }
        return null; // No cover image for other types
    }

    private boolean isYouTubeUrl(String url) {
        return url.contains("youtube.com") || url.contains("youtu.be");
    }

    private String convertToEmbedUrl(String url) {
        String videoId = extractYouTubeVideoId(url);
        if (videoId != null) {
            return "https://www.youtube.com/embed/" + videoId;
        }
        return url;
    }

    private String extractYouTubeVideoId(String url) {
        // Handle youtube.com URLs
        if (url.contains("youtube.com/watch?v=")) {
            int startIndex = url.indexOf("v=") + 2;
            int endIndex = url.indexOf("&", startIndex);
            if (endIndex == -1) endIndex = url.length();
            return url.substring(startIndex, endIndex);
        }
        // Handle youtu.be URLs
        else if (url.contains("youtu.be/")) {
            int startIndex = url.indexOf("youtu.be/") + 9;
            int endIndex = url.indexOf("?", startIndex);
            if (endIndex == -1) endIndex = url.length();
            return url.substring(startIndex, endIndex);
        }
        return null;
    }

    private String getIconForType(String type) {
        switch (type.toLowerCase()) {
            case "video": return "play_circle";
            case "article": return "article";
            case "guide": return "menu_book";
            default: return "link";
        }
    }

    private String getGradientFromForType(String type) {
        switch (type.toLowerCase()) {
            case "video": return "purple-400";
            case "article": return "teal-400";
            case "guide": return "amber-400";
            default: return "blue-400";
        }
    }

    private String getGradientToForType(String type) {
        switch (type.toLowerCase()) {
            case "video": return "purple-600";
            case "article": return "teal-600";
            case "guide": return "amber-600";
            default: return "blue-600";
        }
    }

    private String getBadgeColorForType(String type) {
        switch (type.toLowerCase()) {
            case "video": return "purple";
            case "article": return "teal";
            case "guide": return "amber";
            default: return "blue";
        }
    }}

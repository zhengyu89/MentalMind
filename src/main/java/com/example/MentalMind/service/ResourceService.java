package com.example.MentalMind.service;

import com.example.MentalMind.model.Resource;
import com.example.MentalMind.model.User;
import com.example.MentalMind.model.UserResourceBookmark;
import com.example.MentalMind.repository.ResourceRepository;
import com.example.MentalMind.repository.UserRepository;
import com.example.MentalMind.repository.UserResourceBookmarkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserResourceBookmarkRepository bookmarkRepository;

    /**
     * Get all active resources
     */
    public List<Resource> getAllResources() {
        return resourceRepository.findByIsActiveTrue();
    }

    /**
     * Get resources by type
     */
    public List<Resource> getResourcesByType(String type) {
        return resourceRepository.findByTypeAndIsActiveTrue(type);
    }

    /**
     * Search resources by title
     */
    public List<Resource> searchResources(String query) {
        return resourceRepository.findByTitleContainingIgnoreCaseAndIsActiveTrue(query);
    }

    /**
     * Get resource by ID
     */
    public Optional<Resource> getResourceById(Long id) {
        return resourceRepository.findById(id);
    }

    /**
     * Get user's bookmarks
     */
    public List<UserResourceBookmark> getUserBookmarks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Only return bookmarks that point to active resources
        return bookmarkRepository.findByUserAndIsActiveTrue(user)
                .stream()
                .filter(b -> b.getResource() != null && Boolean.TRUE.equals(b.getResource().getIsActive()))
                .toList();
    }

    /**
     * Add bookmark for user
     */
    @Transactional
    public UserResourceBookmark addBookmark(Long userId, Long resourceId, String bookmarkType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        // Remove any old inactive bookmark rows to avoid duplicates
        bookmarkRepository.findByUserAndResource(user, resource)
                .filter(b -> !Boolean.TRUE.equals(b.getIsActive()))
                .ifPresent(bookmarkRepository::delete);

        // Check if bookmark already exists
        if (bookmarkRepository.existsByUserAndResourceAndBookmarkTypeAndIsActiveTrue(user, resource, bookmarkType)) {
            throw new RuntimeException("Bookmark already exists");
        }

        UserResourceBookmark bookmark = new UserResourceBookmark(user, resource, bookmarkType);
        return bookmarkRepository.save(bookmark);
    }

    /**
     * Remove bookmark
     */
    @Transactional
    public void removeBookmark(Long userId, Long resourceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        Optional<UserResourceBookmark> bookmark = bookmarkRepository.findByUserAndResourceAndIsActiveTrue(user,
                resource);
        if (bookmark.isPresent()) {
            // Hard delete so the row is gone from DB
            bookmarkRepository.delete(bookmark.get());
        }
    }

    /**
     * Create a new resource (for admin use)
     */
    @Transactional
    public Resource createResource(String title, String type, String description, String content,
            String icon, String gradientFrom, String gradientTo, String badgeColor) {
        Resource resource = new Resource(title, type, description, content, icon, gradientFrom, gradientTo, badgeColor);
        return resourceRepository.save(resource);
    }

    /**
     * Create a new resource with cover image (for admin use)
     */
    @Transactional
    public Resource createResource(String title, String type, String description, String content,
            String icon, String gradientFrom, String gradientTo, String badgeColor, String coverImageUrl) {
        Resource resource = new Resource(title, type, description, content, icon, gradientFrom, gradientTo, badgeColor,
                coverImageUrl);
        return resourceRepository.save(resource);
    }

    /**
     * Create a new resource with category and optional cover image
     */
    @Transactional
    public Resource createResource(String title, String type, String description, String content,
            String icon, String gradientFrom, String gradientTo, String badgeColor, String category,
            String coverImageUrl) {
        Resource resource = new Resource(title, type, description, content, icon, gradientFrom, gradientTo, badgeColor,
                coverImageUrl);
        resource.setCategory(category);
        return resourceRepository.save(resource);
    }

    @Transactional
    public void deactivateResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        resource.setIsActive(false);
        resourceRepository.save(resource);

        // Soft-delete related bookmarks so they disappear for users
        List<UserResourceBookmark> bookmarks = bookmarkRepository.findByResourceAndIsActiveTrue(resource);
        for (UserResourceBookmark b : bookmarks) {
            b.setIsActive(false);
            bookmarkRepository.save(b);
        }
    }

    @Transactional
    public Resource updateResource(Long id, String title, String type, String description, String content,
            String icon, String gradientFrom, String gradientTo, String badgeColor, String category,
            String coverImageUrl) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        resource.setTitle(title);
        resource.setType(type);
        resource.setDescription(description);
        resource.setContent(content);
        resource.setIcon(icon);
        resource.setGradientFrom(gradientFrom);
        resource.setGradientTo(gradientTo);
        resource.setBadgeColor(badgeColor);
        resource.setCategory(category);
        resource.setCoverImageUrl(coverImageUrl);
        resource.setUpdatedAt(java.time.LocalDateTime.now());
        return resourceRepository.save(resource);
    }

    /**
     * Get recommended resources based on user's stress level
     * 
     * @param stressLevel LOW, MODERATE, or HIGH
     * @param count       number of resources to return
     * @return shuffled list of recommended resources
     */
    public List<Resource> getRecommendedResources(String stressLevel, int count) {
        List<String> categories;

        switch (stressLevel) {
            case "HIGH":
                categories = List.of("anxiety", "depression", "stress");
                break;
            case "MODERATE":
                categories = List.of("stress", "mindfulness", "relationships");
                break;
            case "LOW":
            default:
                // For LOW stress, return from all categories
                List<Resource> allResources = new ArrayList<>(resourceRepository.findByIsActiveTrue());
                Collections.shuffle(allResources);
                return allResources.subList(0, Math.min(count, allResources.size()));
        }

        List<Resource> resources = new ArrayList<>(resourceRepository.findByCategoryInAndIsActiveTrue(categories));

        // If not enough resources in priority categories, add from all
        if (resources.size() < count) {
            List<Resource> allResources = resourceRepository.findByIsActiveTrue();
            for (Resource r : allResources) {
                if (!resources.contains(r)) {
                    resources.add(r);
                }
                if (resources.size() >= count)
                    break;
            }
        }

        Collections.shuffle(resources);
        return resources.subList(0, Math.min(count, resources.size()));
    }

    /**
     * Get random daily wellness suggestions
     * 
     * @param count number of suggestions to return
     * @return list of wellness suggestion maps with icon, title, description,
     *         bgColor
     */
    public List<Map<String, String>> getRandomWellnessSuggestions(int count) {
        List<Map<String, String>> allSuggestions = new ArrayList<>();

        // Pool of wellness suggestions
        allSuggestions
                .add(createSuggestion("self_improvement", "5-Min Meditation", "Start your day mindfully", "teal"));
        allSuggestions
                .add(createSuggestion("edit_note", "Gratitude Journal", "Write 3 things you're thankful for", "amber"));
        allSuggestions
                .add(createSuggestion("directions_walk", "10-Min Walk", "Get some fresh air and movement", "purple"));
        allSuggestions
                .add(createSuggestion("water_drop", "Stay Hydrated", "Drink at least 8 glasses of water", "blue"));
        allSuggestions
                .add(createSuggestion("bedtime", "Sleep Routine", "Aim for 7-8 hours of quality sleep", "indigo"));
        allSuggestions.add(createSuggestion("restaurant", "Healthy Eating",
                "Include fruits and vegetables in your meals", "green"));
        allSuggestions.add(
                createSuggestion("call", "Connect with Someone", "Call or text a friend or family member", "pink"));
        allSuggestions
                .add(createSuggestion("music_note", "Listen to Music", "Play your favorite relaxing songs", "rose"));
        allSuggestions.add(createSuggestion("local_library", "Read for Pleasure",
                "Spend 15 minutes reading something you enjoy", "orange"));
        allSuggestions
                .add(createSuggestion("spa", "Deep Breathing", "Take 5 slow, deep breaths to calm your mind", "cyan"));
        allSuggestions.add(createSuggestion("nature", "Nature Break", "Spend time outdoors or near plants", "lime"));
        allSuggestions
                .add(createSuggestion("phonelink_off", "Digital Detox", "Take a 1-hour break from screens", "slate"));

        Collections.shuffle(allSuggestions);
        return allSuggestions.subList(0, Math.min(count, allSuggestions.size()));
    }

    private Map<String, String> createSuggestion(String icon, String title, String description, String bgColor) {
        Map<String, String> suggestion = new HashMap<>();
        suggestion.put("icon", icon);
        suggestion.put("title", title);
        suggestion.put("description", description);
        suggestion.put("bgColor", bgColor);
        return suggestion;
    }
}
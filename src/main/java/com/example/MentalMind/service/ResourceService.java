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

import java.util.List;
import java.util.Optional;

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
        return bookmarkRepository.findByUserAndIsActiveTrue(user);
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

        Optional<UserResourceBookmark> bookmark = bookmarkRepository.findByUserAndResourceAndIsActiveTrue(user, resource);
        if (bookmark.isPresent()) {
            bookmark.get().setIsActive(false);
            bookmarkRepository.save(bookmark.get());
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
        Resource resource = new Resource(title, type, description, content, icon, gradientFrom, gradientTo, badgeColor, coverImageUrl);
        return resourceRepository.save(resource);
    }

    /**
     * Create a new resource with category and optional cover image
     */
    @Transactional
    public Resource createResource(String title, String type, String description, String content,
                                 String icon, String gradientFrom, String gradientTo, String badgeColor, String category, String coverImageUrl) {
        Resource resource = new Resource(title, type, description, content, icon, gradientFrom, gradientTo, badgeColor, coverImageUrl);
        resource.setCategory(category);
        return resourceRepository.save(resource);
    }

    @Transactional
    public void deactivateResource(Long id) {
        Resource resource = resourceRepository.findById(id).orElseThrow(() -> new RuntimeException("Resource not found"));
        resource.setIsActive(false);
        resourceRepository.save(resource);
    }

    @Transactional
    public Resource updateResource(Long id, String title, String type, String description, String content,
                                  String icon, String gradientFrom, String gradientTo, String badgeColor, String category, String coverImageUrl) {
        Resource resource = resourceRepository.findById(id).orElseThrow(() -> new RuntimeException("Resource not found"));
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
}
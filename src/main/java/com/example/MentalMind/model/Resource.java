package com.example.MentalMind.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String type; // article, video, guide

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = true)
    private String icon; // material symbols icon name

    @Column(nullable = true)
    private String gradientFrom; // CSS gradient start color

    @Column(nullable = true)
    private String gradientTo; // CSS gradient end color

    @Column(nullable = true)
    private String badgeColor; // CSS color for badge

    @Column(nullable = true)
    private String category; // e.g., Anxiety, Depression

    @Column(nullable = true)
    private String coverImageUrl; // URL for cover image (optional, overrides gradient)

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = true)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    public Resource() {}

    public Resource(String title, String type, String description, String content, String icon, String gradientFrom, String gradientTo, String badgeColor) {
        this.title = title;
        this.type = type;
        this.description = description;
        this.content = content;
        this.icon = icon;
        this.gradientFrom = gradientFrom;
        this.gradientTo = gradientTo;
        this.badgeColor = badgeColor;
        this.category = null;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Resource(String title, String type, String description, String content, String icon, String gradientFrom, String gradientTo, String badgeColor, String coverImageUrl) {
        this.title = title;
        this.type = type;
        this.description = description;
        this.content = content;
        this.icon = icon;
        this.gradientFrom = gradientFrom;
        this.gradientTo = gradientTo;
        this.badgeColor = badgeColor;
        this.coverImageUrl = coverImageUrl;
        this.category = null;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getGradientFrom() { return gradientFrom; }
    public void setGradientFrom(String gradientFrom) { this.gradientFrom = gradientFrom; }

    public String getGradientTo() { return gradientTo; }
    public void setGradientTo(String gradientTo) { this.gradientTo = gradientTo; }

    public String getBadgeColor() { return badgeColor; }
    public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
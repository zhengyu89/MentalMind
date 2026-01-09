package com.example.MentalMind.model;

public enum MaterialType {
    VIDEO("Video Link"),
    DOCUMENT("Reading Document"),
    QUIZ("Quiz");
    
    private final String displayName;
    
    MaterialType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

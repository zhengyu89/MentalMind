package com.example.MentalMind.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {
    
    static {
        // Load .env file from root directory
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .directory(".")
                .load();
        
        // Set environment variables from .env file
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }
}

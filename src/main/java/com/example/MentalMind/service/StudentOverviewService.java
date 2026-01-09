package com.example.MentalMind.service;

import com.example.MentalMind.model.MoodEntry;
import com.example.MentalMind.model.SelfAssessmentResult;
import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.MoodEntryRepository;
import com.example.MentalMind.repository.SelfAssessmentRepository;
import com.example.MentalMind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentOverviewService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SelfAssessmentRepository selfAssessmentRepository;

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    /**
     * Convenience overload with no filtering.
     */
    public List<Map<String, Object>> getStudentsOverview() {
        return getStudentsOverview(null, null);
    }

    /**
     * Build a simple overview list of student risk and recent activity
     * for the counselor Students page, with optional search and filter.
     *
     * @param search free-text search on name or email
     * @param filter filter key: null/empty = all, "high", "active", "followup"
     */
    public List<Map<String, Object>> getStudentsOverview(String search, String filter) {
        // Get all users with role "student"
        List<User> students = userRepository.findAll().stream()
                .filter(u -> "student".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();

        for (User student : students) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", student.getId());
            row.put("name", student.getFullName() != null ? student.getFullName() : student.getEmail());
            row.put("email", student.getEmail());

            // Latest self-assessment (if any)
            Optional<SelfAssessmentResult> latestAssessmentOpt =
                    selfAssessmentRepository.findTopByUserOrderByCompletedAtDesc(student);

            String lastAssessmentLabel = "No assessment yet";
            String lastAssessmentDate = "-";
            String riskLevel = "LOW";

            if (latestAssessmentOpt.isPresent()) {
                SelfAssessmentResult assessment = latestAssessmentOpt.get();
                lastAssessmentLabel = "Self-Assessment: " + assessment.getScore();
                lastAssessmentDate = formatDate(assessment.getCompletedAt());
                riskLevel = assessment.getStressLevel() != null
                        ? assessment.getStressLevel().toUpperCase(Locale.ROOT)
                        : "LOW";
            }

            row.put("lastAssessmentLabel", lastAssessmentLabel);
            row.put("lastAssessmentDate", lastAssessmentDate);
            row.put("riskLevel", riskLevel);
            row.put("riskLabel", toRiskLabel(riskLevel));

            // Last active (based on latest mood or assessment)
            LocalDateTime lastActiveTime = null;

            List<MoodEntry> moods = moodEntryRepository.findByUserId(student.getId());
            if (!moods.isEmpty()) {
                lastActiveTime = moods.get(0).getCreatedAt();
            } else if (latestAssessmentOpt.isPresent()) {
                lastActiveTime = latestAssessmentOpt.get().getCompletedAt();
            }

            String lastActiveLabel = lastActiveTime != null ? toRelativeTime(lastActiveTime) : "No activity";
            row.put("lastActive", lastActiveLabel);

            result.add(row);
        }

        // Apply search filter (by name or email)
        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim().toLowerCase(Locale.ROOT);
            result = result.stream()
                    .filter(r -> {
                        String name = Objects.toString(r.get("name"), "").toLowerCase(Locale.ROOT);
                        String email = Objects.toString(r.get("email"), "").toLowerCase(Locale.ROOT);
                        return name.contains(query) || email.contains(query);
                    })
                    .collect(Collectors.toList());
        }

        // Apply dropdown filter
        if (filter != null && !filter.trim().isEmpty()) {
            String f = filter.trim().toLowerCase(Locale.ROOT);
            switch (f) {
                case "high" -> {
                    final String HIGH = "HIGH";
                    result = result.stream()
                            .filter(r -> HIGH.equalsIgnoreCase(Objects.toString(r.get("riskLevel"), "")))
                            .collect(Collectors.toList());
                }
                case "active" -> {
                    // Active this week: label Today, Yesterday or <= 7 days ago
                    result = result.stream()
                            .filter(r -> {
                                String label = Objects.toString(r.get("lastActive"), "");
                                if ("Today".equalsIgnoreCase(label) || "Yesterday".equalsIgnoreCase(label)) {
                                    return true;
                                }
                                if (label.endsWith(" days ago")) {
                                    try {
                                        int days = Integer.parseInt(label.split(" ")[0]);
                                        return days <= 7;
                                    } catch (NumberFormatException ex) {
                                        return false;
                                    }
                                }
                                return false;
                            })
                            .collect(Collectors.toList());
                }
                case "followup" -> {
                    // Pending follow-up: treat HIGH or MODERATE risk as needing follow-up
                    result = result.stream()
                            .filter(r -> {
                                String risk = Objects.toString(r.get("riskLevel"), "");
                                return "HIGH".equalsIgnoreCase(risk) || "MODERATE".equalsIgnoreCase(risk);
                            })
                            .collect(Collectors.toList());
                }
                default -> {
                    // no-op for unknown filter
                }
            }
        }

        // Sort by risk (HIGH first, then MODERATE, then LOW)
        result.sort((a, b) -> {
            String riskA = (String) a.get("riskLevel");
            String riskB = (String) b.get("riskLevel");
            int riskOrderA = riskOrder(riskA);
            int riskOrderB = riskOrder(riskB);
            int cmp = Integer.compare(riskOrderA, riskOrderB);
            if (cmp != 0) return cmp;
            // keep existing order if same risk (no exact timestamp stored)
            return 0;
        });

        return result;
    }

    private int riskOrder(String risk) {
        if ("HIGH".equalsIgnoreCase(risk)) return 0;
        if ("MODERATE".equalsIgnoreCase(risk)) return 1;
        return 2; // LOW or anything else
    }

    private String toRiskLabel(String risk) {
        if ("HIGH".equalsIgnoreCase(risk)) return "High";
        if ("MODERATE".equalsIgnoreCase(risk)) return "Moderate";
        if ("LOW".equalsIgnoreCase(risk)) return "Low";
        return "Unknown";
    }

    private String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return dateTime.format(fmt);
    }

    private String toRelativeTime(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        Duration diff = Duration.between(time, now);
        long days = diff.toDays();
        if (days <= 0) {
            return "Today";
        } else if (days == 1) {
            return "Yesterday";
        } else {
            return days + " days ago";
        }
    }
}



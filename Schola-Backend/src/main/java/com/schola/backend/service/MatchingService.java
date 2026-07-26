package com.schola.backend.service;

import com.schola.backend.entity.Scholarship;
import com.schola.backend.entity.User;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MatchingService {

    // This is the real matching algorithm —
    // calculates a score based on how well
    // the user's profile matches each scholarship
    public int calculateMatch(User user, Scholarship scholarship) {
        int score = 0;
        int total = 0;

        List<String> userTags = user.getTags() != null ? user.getTags() : new java.util.ArrayList<>();

        // ── Field of study match (40 points) ──────────────
        if (scholarship.getFields() != null
                && !scholarship.getFields().isEmpty()
                && user.getTags() != null) {
            total += 40;
            boolean fieldMatch = scholarship.getFields().stream()
                    .anyMatch(f -> user.getTags().stream()
                            .anyMatch(t -> t.equalsIgnoreCase(f)));
            if (fieldMatch) score += 40;
        }

        // ── Trait match (30 points) ────────────────────────
        if (scholarship.getTraits() != null
                && !scholarship.getTraits().isEmpty()
                && user.getTags() != null) {
            total += 30;
            long matchCount = scholarship.getTraits().stream()
                    .filter(t -> user.getTags().stream()
                            .anyMatch(ut -> ut.equalsIgnoreCase(t)))
                    .count();
            double traitScore = (double) matchCount / scholarship.getTraits().size() * 30;
            score += (int) traitScore;
        }

        // ── GPA match (20 points) ──────────────────────────
        if (scholarship.getMinGpa() > 0 && user.getGpa() != null) {
            total += 20;
            try {
                double userGpa = Double.parseDouble(user.getGpa());
                if (userGpa >= scholarship.getMinGpa()) score += 20;
                else if (userGpa >= scholarship.getMinGpa() - 0.5) score += 10;
            } catch (NumberFormatException ignored) {}
        }

        // ── Education level match (10 points) ─────────────
        if (scholarship.getEduLevel() != null && !scholarship.getEduLevel().isEmpty()) {
            total += 10;
            if (scholarship.getEduLevel().equalsIgnoreCase(user.getEdu())) {
                score += 10;
            }
        }

        // ── Return percentage ──────────────────────────────
        if (total == 0) return 75; // default if no criteria
        return Math.min(100, (int) ((double) score / total * 100));
    }

    public List<Scholarship> sortByMatch(List<Scholarship> scholarships, User user) {
        return scholarships.stream()
                .sorted((a,b) ->  calculateMatch(user,b) - calculateMatch(user,a))
                .toList();
    }
}

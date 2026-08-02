package com.schola.backend.controller;

import com.schola.backend.dto.ScholarshipDto;
import com.schola.backend.entity.SavedScholarship;
import com.schola.backend.entity.Scholarship;
import com.schola.backend.entity.User;
import com.schola.backend.repository.SavedScholarshipRepository;
import com.schola.backend.repository.ScholarshipRepository;
import com.schola.backend.repository.UserRepository;
import com.schola.backend.service.MatchingService;
import com.schola.backend.service.ScholarshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SavedController {

    private final SavedScholarshipRepository savedRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipService scholarshipService;
    private final MatchingService matchingService;
    private final UserRepository userRepository;

    // GET /users/me/saved
    @GetMapping("/users/me/saved")
    public ResponseEntity<List<Map<String, Object>>> getSaved(
            @AuthenticationPrincipal User user) {

        if (user.getTags() == null) user.setTags(new ArrayList<>());

        List<Map<String, Object>> result = savedRepository.findByUser(user)
                .stream()
                .map(saved -> {
                    Scholarship s = saved.getScholarship();
                    int match = matchingService.calculateMatch(user, s);
                    String status = saved.getStatus().name()
                            .replace("_", " ");
                    status = Character.toUpperCase(status.charAt(0))
                            + status.substring(1).toLowerCase();
                    return Map.<String, Object>of(
                            "id",       s.getId(),
                            "title",    s.getTitle(),
                            "amount",   s.getAmount(),
                            "deadline", s.getDeadline(),
                            "tags",     s.getTags() != null ? s.getTags() : new ArrayList<>(),
                            "match",    match,
                            "status",   status
                    );
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    // POST /users/me/saved
    @PostMapping("/users/me/saved")
    public ResponseEntity<Map<String, Boolean>> save(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {

        String scholarshipId = body.get("scholarshipId");
        Scholarship scholarship = scholarshipRepository.findById(scholarshipId)
                .orElseThrow(() -> new RuntimeException("Scholarship not found"));

        if (!savedRepository.existsByUserAndScholarship(user, scholarship)) {
            savedRepository.save(SavedScholarship.builder()
                    .user(user)
                    .scholarship(scholarship)
                    .build());

            // Update saved count
            user.setSavedCount(user.getSavedCount() + 1);
            userRepository.save(user);
        }

        return ResponseEntity.ok(Map.of("ok", true));
    }

    // DELETE /users/me/saved/:id
    @DeleteMapping("/users/me/saved/{scholarshipId}")
    public ResponseEntity<Map<String, Boolean>> unsave(
            @AuthenticationPrincipal User user,
            @PathVariable String scholarshipId) {

        Scholarship scholarship = scholarshipRepository.findById(scholarshipId)
                .orElseThrow(() -> new RuntimeException("Scholarship not found"));

        savedRepository.findByUserAndScholarship(user, scholarship)
                .ifPresent(s -> {
                    savedRepository.delete(s);
                    // Update saved count
                    user.setSavedCount(Math.max(0, user.getSavedCount() - 1));
                    userRepository.save(user);
                });

        return ResponseEntity.ok(Map.of("ok", true));
    }

    // POST /users/me/applied/:scholarshipId
    @PostMapping("/users/me/applied/{scholarshipId}")
    public ResponseEntity<Map<String, Object>> markApplied(
            @AuthenticationPrincipal User user,
            @PathVariable String scholarshipId) {

        Scholarship scholarship = scholarshipRepository.findById(scholarshipId)
                .orElseThrow(() -> new RuntimeException("Scholarship not found"));

        // Update saved status to IN_PROGRESS if saved
        savedRepository.findByUserAndScholarship(user, scholarship)
                .ifPresent(saved -> {
                    saved.setStatus(SavedScholarship.Status.IN_PROGRESS);
                    savedRepository.save(saved);
                });

        // If not saved yet, save and mark IN_PROGRESS
        if (!savedRepository.existsByUserAndScholarship(user, scholarship)) {
            savedRepository.save(SavedScholarship.builder()
                    .user(user)
                    .scholarship(scholarship)
                    .status(SavedScholarship.Status.IN_PROGRESS)
                    .build());
        }

        // Increment applied count
        user.setAppliedCount(user.getAppliedCount() + 1);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "appliedCount", user.getAppliedCount()
        ));
    }
}

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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/me/saved")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")

public class SavedController {

    private final SavedScholarshipRepository savedRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipService scholarshipService;
    private final MatchingService matchingService;
    private final UserRepository userRepository;


    // GET /users/me/saved
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getSaved(
            @AuthenticationPrincipal User user) {
        User fullUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Map<String, Object>> result = savedRepository.findByUser(user)
                .stream()
                .map(saved -> {
                    ScholarshipDto dto = scholarshipService.toDto(
                            saved.getScholarship(),
                            matchingService.calculateMatch(fullUser, saved.getScholarship())
                    );
                    return Map.of(
                            "id", dto.getId(),
                            "title", dto.getTitle(),
                            "amount", dto.getAmount(),
                            "deadline", dto.getDeadline(),
                            "tags", dto.getTags(),
                            "match", dto.getMatch(),
                            "status", saved.getStatus().name()
                                    .replace("_", " ")
                                    .charAt(0) + saved.getStatus().name()
                                    .replace("_", " ")
                                    .substring(1).toLowerCase()
                    );
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    // POST /users/me/saved
    @PostMapping
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
        }

        return ResponseEntity.ok(Map.of("ok", true));
    }

    // DELETE /users/me/saved/:id
    @DeleteMapping("/{scholarshipId}")
    public ResponseEntity<Map<String, Boolean>> unsave(
            @AuthenticationPrincipal User user,
            @PathVariable String scholarshipId) {

        Scholarship scholarship = scholarshipRepository.findById(scholarshipId)
                .orElseThrow(() -> new RuntimeException("Scholarship not found"));

        savedRepository.findByUserAndScholarship(user, scholarship)
                .ifPresent(savedRepository::delete);

        return ResponseEntity.ok(Map.of("ok", true));
    }
}
package com.schola.backend.controller;

import com.schola.backend.dto.AuthResponse;
import com.schola.backend.dto.ScholarshipDto;
import com.schola.backend.entity.User;
import com.schola.backend.repository.UserRepository;
import com.schola.backend.service.AuthService;
import com.schola.backend.service.ScholarshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final ScholarshipService scholarshipService;

    // GET /users/me/profile
    @GetMapping("/profile")
    public ResponseEntity<AuthResponse.UserDto> getProfile(
            @AuthenticationPrincipal User user) {
        if (user.getTags() == null) {
            user.setTags(new ArrayList<>());
            userRepository.save(user);
        }
        return ResponseEntity.ok(authService.buildUserDto(user));
    }

    // PATCH /users/me/profile
    @PatchMapping("/profile")
    public ResponseEntity<AuthResponse.UserDto> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> updates) {

        if (updates.containsKey("gpa"))
            user.setGpa((String) updates.get("gpa"));
        if (updates.containsKey("major"))
            user.setMajor((String) updates.get("major"));
        if (updates.containsKey("grad"))
            user.setGrad((String) updates.get("grad"));
        if (updates.containsKey("edu"))
            user.setEdu((String) updates.get("edu"));

        // Recalculate completion
        int completion = 50;
        if (user.getTags() != null && !user.getTags().isEmpty()) completion += 20;
        if (user.getEdu() != null) completion += 10;
        if (user.getGpa() != null) completion += 10;
        if (user.getMajor() != null) completion += 5;
        if (user.getGrad() != null) completion += 5;
        user.setCompletion(Math.min(100, completion));

        User saved = userRepository.save(user);
        return ResponseEntity.ok(authService.buildUserDto(saved));
    }

    // GET /users/me/stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @AuthenticationPrincipal User user) {

        if (user.getTags() == null) {
            user.setTags(new ArrayList<>());
        }

        // Real match count from database
        int matchCount = scholarshipService.getMatchesForUser(user).size();

        // Real available amount — sum of all matching scholarships
        long availableAmount = scholarshipService
                .getMatchesForUser(user)
                .stream()
                .mapToLong(ScholarshipDto::getAmount)
                .sum();

        return ResponseEntity.ok(Map.of(
                "matches", matchCount,
                "available", availableAmount,
                "applied", user.getAppliedCount()
        ));
    }

    // POST /users/me/onboarding
    @PostMapping("/onboarding")
    public ResponseEntity<AuthResponse.UserDto> submitOnboarding(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {
        user.setOnboarded(true);
        user.setCompletion(78);
        User saved = userRepository.save(user);
        return ResponseEntity.ok(authService.buildUserDto(saved));
    }
}
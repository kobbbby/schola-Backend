package com.schola.backend.controller;

import com.schola.backend.dto.ScholarshipDto;
import com.schola.backend.entity.User;
import com.schola.backend.service.ScholarshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScholarshipController {

    private final ScholarshipService scholarshipService;

    // GET /users/me/matches
    @GetMapping("/users/me/matches")
    public ResponseEntity<List<ScholarshipDto>> getMatches(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(scholarshipService.getMatchesForUser(user));
    }

    // GET /scholarships/trending
    @GetMapping("/scholarships/trending")
    public ResponseEntity<List<ScholarshipDto>> getTrending(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(scholarshipService.getAllScholarships());
    }

    // GET /scholarships/search?q=...
    @GetMapping("/scholarships/search")
    public ResponseEntity<List<ScholarshipDto>> search(
            @RequestParam String q,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(scholarshipService.search(q, user));
    }

    // GET /scholarships/:id
    @GetMapping("/scholarships/{id}")
    public ResponseEntity<ScholarshipDto> getById(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(scholarshipService.getById(id,user));
    }
}

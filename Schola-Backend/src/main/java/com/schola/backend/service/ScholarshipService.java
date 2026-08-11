package com.schola.backend.service;

import com.schola.backend.dto.ScholarshipDto;
import com.schola.backend.entity.Scholarship;
import com.schola.backend.entity.User;
import com.schola.backend.repository.ScholarshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final MatchingService matchingService;

    public List<ScholarshipDto> getMatchesForUser(User user) {
        if (user == null) return new ArrayList<>();
        if (user.getTags() == null) user.setTags(new ArrayList<>());

        try {
            List<Scholarship> all = scholarshipRepository.findByActiveTrue();
            if (all == null || all.isEmpty()) return new ArrayList<>();

            return all.stream()
                    .map(s -> toDto(s, matchingService.calculateMatch(user, s)))
                    .filter(s -> s.getMatch() >= 50)
                    .sorted((a, b) -> b.getMatch() - a.getMatch())
                    .toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<ScholarshipDto> getAllScholarships() {
        return scholarshipRepository.findByActiveTrue()
                .stream()
                .map(s -> toDto(s, 0))
                .toList();
    }

    public ScholarshipDto getById(String id, User user) {
        Scholarship s = scholarshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scholarship not found"));
        int match = 0;
        if (user != null) {
            if (user.getTags() == null) user.setTags(new ArrayList<>());
            match = matchingService.calculateMatch(user, s);
        }
        return toDto(s, match);
    }

    public List<ScholarshipDto> search(String query, User user) {
        if (user != null && user.getTags() == null) {
            user.setTags(new ArrayList<>());
        }
        return scholarshipRepository.searchByTitle(query)
                .stream()
                .map(s -> {
                    int match = user != null
                            ? matchingService.calculateMatch(user, s)
                            : 0;
                    return toDto(s, match);
                })
                .toList();
    }

    public ScholarshipDto toDto(Scholarship s, int match) {
        return ScholarshipDto.builder()
                .id(s.getId())
                .title(s.getTitle())
                .amount(s.getAmount())
                .deadline(s.getDeadline())
                .tags(s.getTags() != null ? s.getTags() : new ArrayList<>())
                .match(match)
                .about(s.getAbout())
                .eligibility(s.getEligibility())
                .requirements(s.getRequirements())
                .applicationUrl(s.getApplicationUrl())
                .build();
    }
}
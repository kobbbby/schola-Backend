package com.schola.backend.service;

import com.schola.backend.dto.ScholarshipDto;
import com.schola.backend.entity.Scholarship;
import com.schola.backend.entity.User;
import com.schola.backend.repository.ScholarshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final MatchingService matchingService;

    public List<ScholarshipDto> getMatchesForUser(User user) {
           if(user.getTags() == null){
               user.setTags(new java.util.ArrayList<>());
           }
           List<Scholarship> all = scholarshipRepository.findByActiveTrue();
        List<Scholarship> sorted = matchingService.sortByMatch(all, user);
        return sorted.stream()
                .map(s -> toDto(s, matchingService.calculateMatch(user, s)))
                .filter(s -> s.getMatch() >= 50)
                .toList();
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

        // Calculate real match for this specific user
        int match = 0;
        if (user != null) {
            if (user.getTags() == null) {
                user.setTags(new java.util.ArrayList<>());
            }
            match = matchingService.calculateMatch(user, s);
            return toDto(s, match);
        }
        return toDto(s, match);
    }

    public List<ScholarshipDto> search(String query, User user) {
        return scholarshipRepository.searchByTitle(query)
                .stream()
                .map(s -> toDto(s, user != null ? matchingService.calculateMatch(user, s) : 0))
                .toList();
    }

    public ScholarshipDto toDto(Scholarship s, int match) {
        return ScholarshipDto.builder()
                .id(s.getId())
                .title(s.getTitle())
                .amount(s.getAmount())
                .deadline(s.getDeadline())
                .tags(s.getTags())
                .match(match)
                .about(s.getAbout())
                .eligibility(s.getEligibility())
                .requirements(s.getRequirements())
                .applicationUrl(s.getApplicationUrl())
                .build();
    }
}
package com.schola.backend.controller;

import com.schola.backend.entity.OnboardingStep;
import com.schola.backend.entity.User;
import com.schola.backend.repository.UserRepository;
import com.schola.backend.service.AuthService;
import com.schola.backend.service.ScholarshipService;
import com.schola.backend.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OnboardingController {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final ScholarshipService scholarshipService;

    // GET /onboarding/steps
    @GetMapping("/steps")
    public ResponseEntity<List<OnboardingStep>> getSteps() {
        return ResponseEntity.ok(List.of(
                buildStep("edu", "What's your education level?", false, List.of(
                        new OnboardingStep.OnboardingOption("hs", "High School Senior"),
                        new OnboardingStep.OnboardingOption("ug", "Undergraduate"),
                        new OnboardingStep.OnboardingOption("gr", "Graduate"),
                        new OnboardingStep.OnboardingOption("ph", "PhD")
                )),
                buildStep("fields", "Fields of interest?", true, List.of(
                        new OnboardingStep.OnboardingOption("stem", "STEM"),
                        new OnboardingStep.OnboardingOption("biz", "Business"),
                        new OnboardingStep.OnboardingOption("arts", "Arts & Humanities"),
                        new OnboardingStep.OnboardingOption("med", "Medicine"),
                        new OnboardingStep.OnboardingOption("law", "Law")
                )),
                buildStep("traits", "Which apply to you?", true, List.of(
                        new OnboardingStep.OnboardingOption("fg", "First-Generation"),
                        new OnboardingStep.OnboardingOption("need", "Need-Based"),
                        new OnboardingStep.OnboardingOption("intl", "International"),
                        new OnboardingStep.OnboardingOption("cl", "Community Leader")
                ))
        ));
    }

    // POST /onboarding/submit
    @PostMapping("/submit")
    public ResponseEntity<?> submit(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {

        // Safety check — if user is null token is missing or invalid
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized — please sign in again"));
        }

        // Initialize tags safely
        if (user.getTags() == null) {
            user.setTags(new ArrayList<>());
        }

        List<String> tags = new ArrayList<>(user.getTags());
        String edu = user.getEdu();

        try {
            @SuppressWarnings("unchecked")
            Map<String, List<String>> answers =
                    (Map<String, List<String>>) body.get("answers");

            if (answers != null) {
                List<String> eduAnswer = answers.get("edu");
                if (eduAnswer != null && !eduAnswer.isEmpty()) {
                    String eduId = eduAnswer.get(0);
                    edu = switch (eduId) {
                        case "hs" -> "High School Senior";
                        case "ug" -> "Undergraduate";
                        case "gr" -> "Graduate";
                        case "ph" -> "PhD";
                        default   -> eduId;
                    };
                    if (!tags.contains(edu)) tags.add(edu);
                }

                List<String> fields = answers.get("fields");
                if (fields != null) {
                    for (String f : fields) {
                        String label = switch (f) {
                            case "stem" -> "STEM";
                            case "biz"  -> "Business";
                            case "arts" -> "Arts & Humanities";
                            case "med"  -> "Medicine";
                            case "law"  -> "Law";
                            default     -> f;
                        };
                        if (!tags.contains(label)) tags.add(label);
                    }
                }

                List<String> traits = answers.get("traits");
                if (traits != null) {
                    for (String t : traits) {
                        String label = switch (t) {
                            case "fg"   -> "First-Generation";
                            case "need" -> "Need-Based";
                            case "intl" -> "International";
                            case "cl"   -> "Community Leader";
                            default     -> t;
                        };
                        if (!tags.contains(label)) tags.add(label);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing answers: " + e.getMessage());
        }

        user.setTags(tags);
        if (edu != null) user.setEdu(edu);
        user.setOnboarded(true);

        int completion = 50;
        if (!tags.isEmpty()) completion += 20;
        if (edu != null) completion += 10;
        user.setCompletion(completion);

        User saved = userRepository.save(user);

        try {
            long matchCount = scholarshipService.getMatchesForUser(saved).size();
            saved.setMatchCount((int) matchCount);
            saved = userRepository.save(saved);
        } catch (Exception e) {
            System.out.println("Match error: " + e.getMessage());
        }

        return ResponseEntity.ok(authService.buildUserDto(saved));
    }

    private OnboardingStep buildStep(String id, String label, boolean multi,
                                     List<OnboardingStep.OnboardingOption> options) {
        OnboardingStep step = new OnboardingStep();
        step.setId(id);
        step.setLabel(label);
        step.setMulti(multi);
        step.setOptions(options);
        return step;
    }
}

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
    public ResponseEntity<AuthResponse.UserDto> submit(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {

        @SuppressWarnings("unchecked")
        Map<String, List<String>> answers =
                (Map<String, List<String>>) body.get("answers");

        List<String> tags = new ArrayList<>();
        String edu = null;

        if (answers != null) {
            // Education level
            List<String> eduAnswer = answers.get("edu");
            if (eduAnswer != null && !eduAnswer.isEmpty()) {
                String eduId = eduAnswer.getFirst(); // can change code to get(0) if i first errors
                edu = switch (eduId) {
                    case "hs" -> "High School Senior";
                    case "ug" -> "Undergraduate";
                    case "gr" -> "Graduate";
                    case "ph" -> "PhD";
                    default   -> eduId;
                };
                tags.add(edu);
            }

            // Fields of interest
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
                    tags.add(label);
                }
            }

            // Traits
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
                    tags.add(label);
                }
            }
        }

        // Save everything to the user
        user.setTags(tags);
        if (edu != null) user.setEdu(edu);
        user.setOnboarded(true);

        // Calculate real profile completion
        int completion = 50; // base
        if (!tags.isEmpty()) completion += 20;
        if (edu != null) completion += 10;
        user.setCompletion(completion);

        // Calculate how many scholarships match this user
        long matchCount = scholarshipService.getMatchesForUser(user).size();
        user.setMatchCount((int) matchCount);

        User saved = userRepository.save(user);
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

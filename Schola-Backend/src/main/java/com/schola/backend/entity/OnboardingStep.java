package com.schola.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "onboarding_steps")
public class OnboardingStep {

    @Id
    private String id;

    @Column(nullable = false)
    private String label;

    @Builder.Default
    private boolean multi = false;

    @ElementCollection
    @CollectionTable(name = "onboarding_options", joinColumns = @JoinColumn(name = "step_id"))
    @OrderColumn(name = "option_order")
    private List<OnboardingOption> options;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnboardingOption {
        private String id;
        private String label;
    }
}

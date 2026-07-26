package com.schola.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "saved_scholarships")

public class SavedScholarship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "scholarship_id", nullable = false)
    private Scholarship scholarship;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.NOT_STARTED;

    @Builder.Default
    private LocalDateTime savedAt = LocalDateTime.now();

    public enum Status {
        NOT_STARTED, IN_PROGRESS, SUBMITTED
    }
}
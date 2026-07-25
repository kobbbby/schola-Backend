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
@Table(name = "scholarships")
public class Scholarship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private String deadline;

    @ElementCollection
    @CollectionTable(name = "scholarship_tags", joinColumns = @JoinColumn(name = "scholarship_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Column(length = 2000)
    private String about;

    @Column(length = 2000)
    private String eligibility;

    @Column(length = 2000)
    private String requirements;

    private String applicationUrl;

    @ElementCollection
    @CollectionTable(name = "scholarship_fields", joinColumns = @JoinColumn(name = "scholarship_id"))
    @Column(name = "field")
    private List<String> fields;

    @ElementCollection
    @CollectionTable(name = "scholarship_traits", joinColumns = @JoinColumn(name = "scholarship_id"))
    @Column(name = "trait")
    private List<String> traits;

    private double minGpa;
    private String eduLevel;
    private boolean needBased;

    @Builder.Default
    private boolean active = true;
}
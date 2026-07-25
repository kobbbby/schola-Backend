package com.schola.backend.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(exclude = "tags")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String initials;

    @Builder.Default
    private int completion = 45;

    private String edu;
    private String major;
    private String gpa;
    private String grad;

    @Builder.Default
    private boolean onboarded = false;

    @ElementCollection
    @CollectionTable(name = "user_tags", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "tag")

    private  List<String> tags = new ArrayList<>();

    @Builder.Default
    private int matchCount = 0;

    @Builder.Default
    private int savedCount = 0;

    @Builder.Default
    private int appliedCount = 0;

    @Builder.Default
    private int wonCount = 0;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

}

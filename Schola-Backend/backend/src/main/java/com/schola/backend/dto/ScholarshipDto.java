package com.schola.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipDto {
    private String id;
    private String title;
    private int amount;
    private String deadline;
    private List<String> tags;
    private int match;
    private String about;
    private String eligibility;
    private String requirements;
    private String applicationUrl;
}

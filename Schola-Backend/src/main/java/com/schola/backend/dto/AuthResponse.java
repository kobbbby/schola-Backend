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
public class AuthResponse {

    private String token;
    private UserDto user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private String id;
        private String firstName;
        private String lastName;
        private String name; // leaving this guy so it doesn't break cos i still need to update the frontend
        private String email;
        private String initials;
        private int completion;
        private List<String> tags;
        private String edu;
        private String major;
        private String gpa;
        private String grad;
        private boolean onboarded;
        private Stats stats;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private int matches;
        private int saved;
        private int applied;
        private int won;
    }
}

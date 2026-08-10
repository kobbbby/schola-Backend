package com.schola.backend.service;

import com.schola.backend.dto.AuthRequest;
import com.schola.backend.dto.AuthResponse;
import com.schola.backend.entity.User;
import com.schola.backend.repository.UserRepository;
import com.schola.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse signUp(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists.");
        }

        String firstName = extractFirstName(request);
        String lastName = extractLastName(request);
        String fullName = buildFullName(firstName, lastName, request.getName());

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .name(fullName)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .initials(generateInitials(fullName))
                .onboarded(false)
                .completion(45)
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getId());

        return buildAuthResponse(token, saved);
    }

    public AuthResponse signIn(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with this email."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect password.");
        }

        String token = jwtUtil.generateToken(user.getId());
        return buildAuthResponse(token, user);
    }

    private String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "??";
        String[] parts = name.trim().split(" ");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString().toUpperCase().substring(0, Math.min(2, initials.length()));
    }

    private String extractFirstName(AuthRequest request) {
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            return request.getFirstName().trim();
        }
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String[] parts = request.getName().trim().split(" ");
            return parts.length > 0 ? parts[0] : request.getName().trim();
        }
        return null;
    }

    private String extractLastName(AuthRequest request) {
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            return request.getLastName().trim();
        }
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String[] parts = request.getName().trim().split(" ");
            if (parts.length > 1) {
                StringBuilder last = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    if (last.length() > 0) {
                        last.append(" ");
                    }
                    last.append(parts[i]);
                }
                return last.toString();
            }
        }
        return null;
    }

    private String buildFullName(String firstName, String lastName, String fallbackName) {
        String combined = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        if (!combined.isEmpty()) {
            return combined;
        }
        return fallbackName != null ? fallbackName.trim() : "";
    }
    public AuthResponse.UserDto buildUserDto(User user) {
        return AuthResponse.UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .name(user.getFullName())
                .email(user.getEmail())
                .initials(user.getInitials())
                .completion(user.getCompletion())
                .tags(user.getTags() != null ? user.getTags() : new java.util.ArrayList<>())
                .edu(user.getEdu())
                .major(user.getMajor())
                .gpa(user.getGpa())
                .grad(user.getGrad())
                .onboarded(user.isOnboarded())
                .stats(AuthResponse.Stats.builder()
                        .matches(user.getMatchCount())
                        .saved(user.getSavedCount())
                        .applied(user.getAppliedCount())
                        .won(user.getWonCount())
                        .build())
                .build();
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .user(buildUserDto(user))
                .build();
    }
}

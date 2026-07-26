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

        String initials = generateInitials(request.getName());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .initials(initials)
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
    public AuthResponse.UserDto buildUserDto(User user) {
        return AuthResponse.UserDto.builder()
                .id(user.getId())
                .name(user.getName())
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

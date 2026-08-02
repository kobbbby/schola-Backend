package com.schola.backend.controller;

import com.schola.backend.entity.User;
import com.schola.backend.repository.UserRepository;
import com.schola.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // In-memory OTP store — replace with Redis in production
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();

    // POST /auth/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email is required"));
        }

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal if email exists — security best practice
            return ResponseEntity.ok(Map.of(
                    "message", "If this email exists, an OTP has been sent"
            ));
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(email, otp);

        // In production send via email — for now log it

        // TODO: integrate SendGrid or similar email service
        // emailService.sendOtp(email, otp);

        return ResponseEntity.ok(Map.of(
                "message", "OTP sent successfully",
                "otp", otp // REMOVE this in production — only for testing
        ));
    }

    // POST /auth/verify-otp
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");
        String otp   = body.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email and OTP are required"));
        }

        String stored = otpStore.get(email);
        if (stored == null || !stored.equals(otp)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or expired OTP"));
        }

        // OTP valid — generate a reset token
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String resetToken = jwtUtil.generateToken(user.getId());

        // Remove OTP after use
        otpStore.remove(email);

        return ResponseEntity.ok(Map.of(
                "message", "OTP verified",
                "resetToken", resetToken
        ));
    }

    // POST /auth/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestBody Map<String, String> body) {

        String resetToken   = body.get("resetToken");
        String newPassword  = body.get("newPassword");

        if (resetToken == null || newPassword == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token and password are required"));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password must be at least 6 characters"));
        }

        if (!jwtUtil.isTokenValid(resetToken)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or expired reset token"));
        }

        String userId = jwtUtil.extractUserId(resetToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Password reset successfully"
        ));
    }
}

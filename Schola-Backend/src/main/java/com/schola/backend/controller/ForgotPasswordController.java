package com.schola.backend.controller;

import com.schola.backend.entity.User;
import com.schola.backend.repository.UserRepository;
import com.schola.backend.security.JwtUtil;
import com.schola.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
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
    private final EmailService emailService;

    // In-memory OTP store
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();

    // ── POST /auth/forgot-password ────────────────────────
    // User types their account email → OTP sent to that email
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email is required"));
        }

        // Check if account exists
        var userOpt = userRepository.findByEmail(email.toLowerCase().trim());

        if (userOpt.isEmpty()) {
            // Don't reveal whether email exists — security best practice
            return ResponseEntity.ok(Map.of(
                    "message", "If an account exists with this email, an OTP has been sent"
            ));
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Store OTP against the email
        otpStore.put(email.toLowerCase().trim(), otp);

        // Send OTP to the user's account email via SendGrid
        try {
            emailService.sendOtp(email, otp);
            System.out.println("OTP sent to: " + email);
        } catch (Exception e) {
            System.err.println("SendGrid failed: " + e.getMessage());
            // Fallback — print to console for testing
            System.out.println("FALLBACK OTP for " + email + " : " + otp);
        }

        return ResponseEntity.ok(Map.of(
                "message", "OTP sent to " + email
        ));
    }

    // ── POST /auth/verify-otp ─────────────────────────────
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");
        String otp   = body.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email and OTP are required"));
        }

        String stored = otpStore.get(email.toLowerCase().trim());

        if (stored == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "OTP expired or not found. Please request a new one"));
        }

        if (!stored.equals(otp.trim())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Incorrect OTP. Please try again"));
        }

        // OTP is correct — get the user
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate a short-lived reset token
        String resetToken = jwtUtil.generateToken(user.getId());

        // Remove OTP so it can't be reused
        otpStore.remove(email.toLowerCase().trim());

        return ResponseEntity.ok(Map.of(
                "message", "OTP verified successfully",
                "resetToken", resetToken
        ));
    }

    // ── POST /auth/reset-password ─────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestBody Map<String, String> body) {

        String resetToken  = body.get("resetToken");
        String newPassword = body.get("newPassword");

        if (resetToken == null || newPassword == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Reset token and new password are required"));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password must be at least 6 characters"));
        }

        if (!jwtUtil.isTokenValid(resetToken)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Reset link expired. Please request a new OTP"));
        }

        String userId = jwtUtil.extractUserId(resetToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update the password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Send confirmation email
        try {
            emailService.sendPasswordResetConfirmation(user.getEmail(), user.getName());
        } catch (Exception e) {
            System.err.println("Confirmation email failed: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "message", "Password reset successfully. You can now sign in"
        ));
    }
}
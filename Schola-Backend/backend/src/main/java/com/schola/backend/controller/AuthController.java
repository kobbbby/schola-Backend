package com.schola.backend.controller;

import com.schola.backend.dto.AuthRequest;
import com.schola.backend.dto.AuthResponse;
import com.schola.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    // POST /auth/signup
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    // POST /auth/signin
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.signIn(request));
    }

    // POST /auth/signout
    @PostMapping("/signout")
    public ResponseEntity<String> signOut() {
        // JWT is stateless — client just deletes the token
        return ResponseEntity.ok("Signed out successfully");
    }
}
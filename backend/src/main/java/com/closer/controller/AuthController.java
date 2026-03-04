package com.closer.controller;

import com.closer.dto.AuthRequest;
import com.closer.dto.AuthResponse;
import com.closer.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request.getEmail(), request.getPassword(), request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @GetMapping("/oauth2/callback")
    public ResponseEntity<String> oauthCallback() {
        return ResponseEntity.ok("OAuth2 authentication handled by Spring Security");
    }

    @lombok.Data
    public static class RegisterRequest {
        @jakarta.validation.constraints.NotBlank
        private String name;
        @jakarta.validation.constraints.Email
        @jakarta.validation.constraints.NotBlank
        private String email;
        @jakarta.validation.constraints.Size(min = 6)
        @jakarta.validation.constraints.NotBlank
        private String password;
    }
}

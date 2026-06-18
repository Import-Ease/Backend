package com.example.ImportEase.controllers;

import com.example.ImportEase.dtos.AuthDtos.AuthResponse;
import com.example.ImportEase.dtos.AuthDtos.LoginRequest;
import com.example.ImportEase.dtos.AuthDtos.RegisterRequest;
import com.example.ImportEase.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Spring automatically injects your AuthService here
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Hands the validated request to the service and returns the JWT
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Hands the login credentials to the service and returns the JWT
        return ResponseEntity.ok(authService.login(request));
    }
}
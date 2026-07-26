package com.example.importease.controller;

import com.example.importease.config.LoggingFilter;
import com.example.importease.model.AppUser;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.service.JwtService;
import com.example.importease.model.dto.LoginRequest;
import com.example.importease.dto.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthController(JwtService jwtService, AppUserRepository appUserRepository) {
        this.jwtService = jwtService;
        this.appUserRepository = appUserRepository;
    }

    @Operation(summary = "Register a new account")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (appUserRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Registration failed: username taken {} | correlationId={}", request.getUsername(), LoggingFilter.correlationId());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already taken."));
        }

        if (request.getEmail() != null && appUserRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: email taken {} | correlationId={}", request.getEmail(), LoggingFilter.correlationId());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already registered."));
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "IMPORTER");
        user.setPasswordSet(true);
        user.setCreatedAt(LocalDateTime.now());
        user = appUserRepository.save(user);

        String jwtSubject = user.getEmail() != null ? user.getEmail() : user.getUsername();
        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(jwtSubject)
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();

        String token = jwtService.generateToken(userDetails);

        log.info("User registered: username={} | email={} | role={} | correlationId={}",
                user.getUsername(), user.getEmail(), user.getRole(), LoggingFilter.correlationId());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Registration successful!",
                "accessToken", token,
                "username", user.getUsername(),
                "role", user.getRole(),
                "userId", user.getId()
        ));
    }

    @Operation(summary = "Login with username or email and password")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String identifier = request.getUsername().trim();

        Optional<AppUser> userOpt = appUserRepository.findByUsernameOrEmail(identifier);

        if (userOpt.isEmpty() || !userOpt.get().isPasswordSet()) {
            log.warn("Login failed: unknown user {} | correlationId={}", identifier, LoggingFilter.correlationId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password."));
        }

        AppUser user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: wrong password for {} | correlationId={}", identifier, LoggingFilter.correlationId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password."));
        }

        String jwtSubject = user.getEmail() != null ? user.getEmail() : user.getUsername();
        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(jwtSubject)
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();

        String token = jwtService.generateToken(userDetails);

        log.info("User logged in: username={} | role={} | correlationId={}",
                user.getUsername(), user.getRole(), LoggingFilter.correlationId());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful!",
                "accessToken", token,
                "username", user.getUsername(),
                "role", user.getRole(),
                "userId", user.getId()
        ));
    }
}
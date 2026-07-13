package com.example.importease.controller;

import com.example.importease.model.dto.VerifyOtpRequest;
import com.example.importease.model.AppUser;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.service.OtpService;
import com.example.importease.service.JwtService;
import com.example.importease.model.dto.SetCredentialsRequest;
import com.example.importease.model.dto.LoginRequest;
import com.example.importease.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and OTP endpoints")
public class AuthController {

    private final OtpService otpService;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthController(OtpService otpService, JwtService jwtService, AppUserRepository appUserRepository) {
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.appUserRepository = appUserRepository;
    }

    /**
     * Endpoint to request an OTP via Phone Number
     */
    @Operation(summary = "Request OTP for phone number")
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestParam String phoneNumber) {
        String response = otpService.generateAndSendOtp(phoneNumber);
        return ResponseEntity.ok(Map.of("message", response));
    }

    /**
     * Endpoint to request an OTP via Email
     */
    @Operation(summary = "Request OTP for email")
    @PostMapping("/request-email-otp")
    public ResponseEntity<?> requestEmailOtp(@RequestParam String email) {
        String response = otpService.generateAndSendEmailOtp(email);
        return ResponseEntity.ok(Map.of("message", response));
    }

    /**
     * Endpoint to verify the OTP and log the user in or auto-register them
     */
    @Operation(summary = "Verify OTP and sign in")
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtpAndLogin(@Valid @RequestBody VerifyOtpRequest request) {
        boolean isValid = otpService.verifyOtp(request.getIdentifier(), request.getOtpCode());

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired OTP verification code."));
        }

        boolean isEmail = request.getIdentifier().contains("@");
        Optional<AppUser> userOpt = appUserRepository.findByEmail(request.getIdentifier());

        AppUser user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = new AppUser();
            if (isEmail) {
                user.setEmail(request.getIdentifier());
                user.setPhoneNumber("NOT_PROVIDED_" + UUID.randomUUID().toString().substring(0, 8));
            } else {
                user.setPhoneNumber(request.getIdentifier());
                user.setEmail("NOT_PROVIDED_" + UUID.randomUUID().toString().substring(0, 8) + "@importease.com");
            }
            user.setPassword(UUID.randomUUID().toString()); // Strong random placeholder password
            user.setRole("IMPORTER"); // Assigning default role of Importer
            user.setCreatedAt(LocalDateTime.now());
            user = appUserRepository.save(user);
        }

        UserDetails userDetails = User.withUsername(request.getIdentifier())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(Map.of(
                "message", "Verification successful!",
                "accessToken", token,
                "identifier", request.getIdentifier(),
                "role", user.getRole(),
                "userId", user.getId()
        ));
    }

    /**
     * Called AFTER successful OTP verification, to let user set username+password
     */
    @Operation(summary = "Set username and password after OTP verification")
    @PostMapping("/set-credentials")
    public ResponseEntity<?> setCredentials(@Valid @RequestBody SetCredentialsRequest request) {
        Optional<AppUser> userOpt = appUserRepository.findByEmail(request.getIdentifier());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No verified account found for this identifier. Please verify OTP first."));
        }

        if (appUserRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already taken."));
        }

        AppUser user = userOpt.get();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordSet(true);
        appUserRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Username and password set successfully! You can now log in with either method."));
    }

    /**
     * Traditional username + password login
     */
    @Operation(summary = "Register a new account")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (appUserRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already taken."));
        }

        if (request.getEmail() != null && appUserRepository.findByEmail(request.getEmail()).isPresent()) {
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

        UserDetails userDetails = User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Registration successful!",
                "accessToken", token,
                "username", user.getUsername(),
                "role", user.getRole(),
                "userId", user.getId()
        ));
    }

    @Operation(summary = "Login with username and password")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<AppUser> userOpt = appUserRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty() || !userOpt.get().isPasswordSet()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password."));
        }

        AppUser user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password."));
        }

        UserDetails userDetails = User.withUsername(
                        user.getEmail() != null ? user.getEmail() : user.getPhoneNumber())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(Map.of(
                "message", "Login successful!",
                "accessToken", token,
                "username", user.getUsername(),
                "role", user.getRole(),
                "userId", user.getId()
        ));
    }
}

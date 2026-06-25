package com.example.ImportEase.controllers;

import com.example.ImportEase.dtos.VerifyOtpRequest;
import com.example.ImportEase.models.AppUser;
import com.example.ImportEase.repositories.AppUserRepository;
import com.example.ImportEase.services.OtpService;
import com.example.ImportEase.services.JwtService;
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
}
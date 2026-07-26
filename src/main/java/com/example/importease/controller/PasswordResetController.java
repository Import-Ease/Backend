package com.example.importease.controller;

import com.example.importease.dto.PasswordResetRequest;
import com.example.importease.dto.ResetPasswordRequest;
import com.example.importease.model.AppUser;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    private final AppUserRepository appUserRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(AppUserRepository appUserRepository, OtpService otpService, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody PasswordResetRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        Optional<AppUser> userOpt = appUserRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "If a matching account exists, a reset code has been sent."));
        }

        otpService.generateAndSendEmailOtp(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "If a matching account exists, a reset code has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank() || request.getOtpCode() == null || request.getOtpCode().isBlank() || request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email, OTP, and new password are required"));
        }

        if (!otpService.verifyOtp(request.getEmail(), request.getOtpCode())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired OTP"));
        }

        Optional<AppUser> userOpt = appUserRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        AppUser user = userOpt.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordSet(true);
        appUserRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }
}

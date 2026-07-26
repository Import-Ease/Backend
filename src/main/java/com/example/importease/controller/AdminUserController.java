package com.example.importease.controller;

import com.example.importease.config.LoggingFilter;
import com.example.importease.model.AppUser;
import com.example.importease.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<AppUser>> getAllUsers(@RequestParam(required = false) String query) {
        if (query != null && !query.isBlank()) {
            return ResponseEntity.ok(userRepository.searchUsers(query.trim()));
        }
        return ResponseEntity.ok(userRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUser> getUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        var user = userRepository.findById(id);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        boolean current = user.get().isEnabled();
        boolean newStatus = body.getOrDefault("enabled", current);
        user.get().setEnabled(newStatus);
        userRepository.save(user.get());
        log.info("User status toggled: id={} {} -> {} | correlationId={}",
                id, current, newStatus, LoggingFilter.correlationId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        if (newRole == null || newRole.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        var user = userRepository.findById(id);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        String prevRole = user.get().getRole();
        user.get().setRole(newRole.toUpperCase());
        userRepository.save(user.get());
        log.info("User role updated: id={} {} -> {} | correlationId={}",
                id, prevRole, newRole.toUpperCase(), LoggingFilter.correlationId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().build();
        }
        var user = userRepository.findById(id);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        user.get().setPassword(passwordEncoder.encode(newPassword));
        user.get().setPasswordSet(true);
        userRepository.save(user.get());
        log.info("User password reset: id={} | correlationId={}", id, LoggingFilter.correlationId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("User deleted: id={} | correlationId={}", id, LoggingFilter.correlationId());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

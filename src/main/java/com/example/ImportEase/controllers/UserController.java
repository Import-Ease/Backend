package com.example.ImportEase.controllers;

import com.example.ImportEase.models.AppUser;
import com.example.ImportEase.repositories.AppUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AppUserRepository userRepository;

    public UserController(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * GET /api/users/profile - Fetch profile details of the logged-in user
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Principal principal) {
        AppUser user = userRepository.findByEmail(principal.getName())
                .or(() -> userRepository.findByPhoneNumber(principal.getName()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(user);
    }

    /**
     * PUT /api/users/profile - Update user profile information
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates, Principal principal) {
        AppUser user = userRepository.findByEmail(principal.getName())
                .or(() -> userRepository.findByPhoneNumber(principal.getName()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Allow updating optional details
        if (updates.containsKey("fullName")) {
            user.setFullName(updates.get("fullName"));
        }
        if (updates.containsKey("companyName")) {
            user.setCompanyName(updates.get("companyName"));
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully!",
                "fullName", user.getFullName() != null ? user.getFullName() : "",
                "companyName", user.getCompanyName() != null ? user.getCompanyName() : ""
        ));
    }
}

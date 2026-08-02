package com.example.importease.controller;

import com.example.importease.model.AppUser;
import com.example.importease.model.Supplier;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.repository.ShipmentRepository;
import com.example.importease.repository.SupplierRepository;
import com.example.importease.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile endpoints")
public class AppUserController {

    private final AppUserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final SupplierRepository supplierRepository;
    private final CloudinaryService cloudinaryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AppUserController(AppUserRepository userRepository, ShipmentRepository shipmentRepository,
                             SupplierRepository supplierRepository, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.shipmentRepository = shipmentRepository;
        this.supplierRepository = supplierRepository;
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * GET /api/users/profile - Fetch profile details of the logged-in user
     */
    @Operation(summary = "Get user profile")
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Principal principal) {
        AppUser user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(user);
    }

    /**
     * PUT /api/users/profile - Update user profile information
     */
    @Operation(summary = "Update user profile")
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates, Principal principal) {
        AppUser user = userRepository.findByEmail(principal.getName())
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

    /**
     * PUT /api/users/change-password - Update the logged-in user's password.
     * Requires the current password for confirmation.
     */
    @Operation(summary = "Change my password")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Principal principal) {
        AppUser user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String current = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        String confirm = body.get("confirmPassword");

        if (current == null || newPassword == null || confirm == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "All fields are required."));
        }
        if (!newPassword.equals(confirm)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "New passwords do not match."));
        }
        if (newPassword.length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "New password must be at least 8 characters."));
        }
        if (!passwordEncoder.matches(current, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Current password is incorrect."));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordSet(true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }

    /**
     * DELETE /api/users/me - Permanently delete the logged-in user's account
     * Requires password confirmation for safety.
     */
    @Operation(summary = "Delete my account")
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMyAccount(@RequestBody Map<String, String> body, Principal principal) {
        AppUser user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String password = body.get("password");
        if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Incorrect password. Account not deleted."));
        }

        // Clean up Cloudinary images if this user is a supplier
        supplierRepository.findByOwnerId(user.getId()).ifPresent(supplier -> {
            if (supplier.getProducts() != null) {
                for (com.example.importease.model.Product product : supplier.getProducts()) {
                    String publicId = cloudinaryService.extractPublicId(product.getImageUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteImage(publicId);
                    }
                }
            }
        });

        // Delete the user's shipments first to avoid foreign key constraint errors
        shipmentRepository.findByUser(user).forEach(shipmentRepository::delete);

        userRepository.delete(user);

        return ResponseEntity.ok(Map.of("message", "Account deleted successfully."));
    }
}
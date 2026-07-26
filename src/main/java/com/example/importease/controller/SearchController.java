package com.example.importease.controller;

import com.example.importease.model.AppUser;
import com.example.importease.model.SearchLog;
import com.example.importease.model.SearchResponseDto;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.repository.ProductRepository;
import com.example.importease.service.SearchLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SearchLogService searchLogService;

    @Autowired
    private AppUserRepository appUserRepository;

    @GetMapping
    public ResponseEntity<List<SearchResponseDto>> search(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Log the search for authenticated user (deduplicated)
        if (userDetails != null) {
            appUserRepository.findByUsernameOrEmail(userDetails.getUsername())
                    .ifPresent(user -> searchLogService.logUserSearch(query, user.getId()));
        } else {
            searchLogService.logSearchOnly(query);
        }

        // Return fuzzy matched results
        List<SearchResponseDto> results = productRepository.searchProducts(query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String prefix) {
        List<String> suggestions = productRepository.findTop5NamesByPrefix(prefix);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/history")
    public ResponseEntity<List<SearchLog>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(List.of());
        }
        UUID userId = appUserRepository.findByUsernameOrEmail(userDetails.getUsername())
                .map(AppUser::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(searchLogService.getUserSearchHistory(userId));
    }

    @DeleteMapping("/history/{logId}")
    public ResponseEntity<Void> deleteHistoryItem(
            @PathVariable Long logId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = appUserRepository.findByUsernameOrEmail(userDetails.getUsername())
                .map(AppUser::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        searchLogService.deleteSearchLog(logId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            appUserRepository.findByUsernameOrEmail(userDetails.getUsername())
                    .ifPresent(user -> searchLogService.clearUserSearchHistory(user.getId()));
        }
        return ResponseEntity.noContent().build();
    }
}
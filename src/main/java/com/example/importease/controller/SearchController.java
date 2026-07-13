package com.example.importease.controller;

import com.example.importease.model.SearchResponseDto;
import com.example.importease.repository.ProductRepository;
import com.example.importease.service.SearchLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SearchLogService searchLogService;

    @GetMapping
    public ResponseEntity<List<SearchResponseDto>> search(@RequestParam String query) {
        // Log the search + trigger WebSocket alert
        searchLogService.logSearchOnly(query);

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
    public ResponseEntity<List<String>> getHistory() {
        return ResponseEntity.ok(searchLogService.getRecentSearches());
    }
}
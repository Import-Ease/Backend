package com.example.importease.controller;

import com.example.importease.model.SearchResponseDto;
import com.example.importease.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*") // Allows the mobile app to communicate with your backend securely
public class SearchController {

    @Autowired
    private ProductRepository productRepository;

    // Connects Tuesday's upgraded search engine to the mobile app frontend
    @GetMapping
    public ResponseEntity<List<SearchResponseDto>> search(@RequestParam String query) {
        List<SearchResponseDto> results = productRepository.searchProducts(query);
        return ResponseEntity.ok(results);
    }

    // Connects Wednesday's dropdown auto-suggest engine to the mobile app frontend
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String prefix) {
        List<String> suggestions = productRepository.findTop5NamesByPrefix(prefix);
        return ResponseEntity.ok(suggestions);
    }
}
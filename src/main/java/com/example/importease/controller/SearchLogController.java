package com.example.importease.controller;

import com.example.importease.model.Product;
import com.example.importease.service.SearchLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchLogController {

    @Autowired
    private SearchLogService searchLogService;

    // Endpoint: http://localhost:8080/api/search?query=laptop
    @GetMapping
    public ResponseEntity<List<Product>> searchProducts(@RequestParam("query") String query) {
        List<Product> results = searchLogService.searchProducts(query);
        return ResponseEntity.ok(results);
    }

    // Endpoint: http://localhost:8080/api/search/suggestions?prefix=lap
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam("prefix") String prefix) {
        List<String> suggestions = searchLogService.getSearchSuggestions(prefix);
        return ResponseEntity.ok(suggestions);
    }
}
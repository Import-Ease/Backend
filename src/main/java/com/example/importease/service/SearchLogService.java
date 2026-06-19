package com.example.importease.service;

import com.example.importease.model.Product;
import com.example.importease.model.SearchLog;
import com.example.importease.repository.ProductRepository;
import com.example.importease.repository.SearchLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchLogService {

    @Autowired
    private SearchLogRepository searchLogRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // --- Search Logic ---
    public List<Product> searchProducts(String query) {
        if (query != null && !query.trim().isEmpty()) {
            String cleanQuery = query.trim();

            // 1. Log the search query to the database history
            searchLogRepository.save(new SearchLog(cleanQuery));

            // 2. Real-Time Communication Specialist Role: Broadcast live alert to the frontend
            messagingTemplate.convertAndSend("/topic/search-alerts", "Live Alert: Someone searched for '" + cleanQuery + "'");
        }

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return productRepository.searchProducts(query);
    }

    // --- Autocomplete Suggestions ---
    public List<String> getSearchSuggestions(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return List.of();
        }
        return productRepository.findTop5NamesByPrefix(prefix.trim());
    }
}
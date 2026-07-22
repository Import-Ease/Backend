package com.example.importease.service;

import com.example.importease.model.SearchLog;
import com.example.importease.repository.SearchLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SearchLogService {

    @Autowired
    private SearchLogRepository searchLogRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Called by SearchController to log + alert without returning results
    public void logSearchOnly(String query) {
        if (query != null && !query.trim().isEmpty()) {
            String cleanQuery = query.trim();
            searchLogRepository.save(new SearchLog(cleanQuery));
            messagingTemplate.convertAndSend("/topic/search-alerts",
                    "Live Alert: Someone searched for " + cleanQuery);
        }
    }

    // Log a search for an authenticated user, deduplicating by merging timestamps
    @Transactional
    public void logUserSearch(String query, UUID userId) {
        if (query == null || query.trim().isEmpty() || userId == null) return;

        String cleanQuery = query.trim();

        Optional<SearchLog> existing = searchLogRepository
                .findByUserIdAndSearchQueryIgnoreCase(userId, cleanQuery);

        if (existing.isPresent()) {
            SearchLog log = existing.get();
            log.setTimestamp(LocalDateTime.now());
            searchLogRepository.save(log);
        } else {
            searchLogRepository.save(new SearchLog(cleanQuery, userId));
        }

        messagingTemplate.convertAndSend("/topic/search-alerts",
                "Live Alert: " + userId + " searched for " + cleanQuery);
    }

    // Get search history for a specific user, newest first
    public List<SearchLog> getUserSearchHistory(UUID userId) {
        return searchLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    // Delete a single history entry (with ownership check)
    @Transactional
    public void deleteSearchLog(Long logId, UUID userId) {
        SearchLog log = searchLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Search log not found"));
        if (!log.getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Not authorized to delete this search log");
        }
        searchLogRepository.delete(log);
    }

    public SearchLog getSearchLogById(Long logId) {
        return searchLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Search log not found"));
    }

    // Clear all search history for a user
    @Transactional
    public void clearUserSearchHistory(UUID userId) {
        searchLogRepository.deleteByUserId(userId);
    }

    public List<String> getRecentSearches() {
        return searchLogRepository.findTop10ByOrderByTimestampDesc().stream()
                .map(SearchLog::getSearchQuery)
                .toList();
    }

    // Autocomplete suggestions
    public List<String> getSearchSuggestions(String prefix) {
        if (prefix != null && !prefix.trim().isEmpty()) {
            return searchLogRepository.findProjectedNamesByPrefix(prefix.trim().toLowerCase());
        }
        return List.of();
    }
}
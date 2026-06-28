package com.example.importease.service;

import com.example.importease.model.SearchLog;
import com.example.importease.model.SearchResponseDto;
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

    // Autocomplete suggestions
    public List<String> getSearchSuggestions(String prefix) {
        if (prefix != null && !prefix.trim().isEmpty()) {
            return searchLogRepository.findProjectedNamesByPrefix(prefix.trim().toLowerCase());
        }
        return List.of();
    }
}
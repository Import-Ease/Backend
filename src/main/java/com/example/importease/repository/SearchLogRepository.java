package com.example.importease.repository;

import com.example.importease.model.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
    // This instantly gives you database functions like .save(), .findAll(), and .delete() out-of-the-box!
}

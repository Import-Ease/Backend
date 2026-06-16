package com.example.importease.repository;

import com.example.importease.model.ImportItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportItemRepository extends JpaRepository<ImportItem, Long> {
}
package com.example.importease.service;

import com.example.importease.model.ImportItem;
import com.example.importease.repository.ImportItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class ImportItemService {

    @Autowired
    private ImportItemRepository repository;

    public List<ImportItem> getAllItems() {
        return repository.findAll();
    }

    public ImportItem saveItem(ImportItem item) {
        return repository.save(item);
    }
}

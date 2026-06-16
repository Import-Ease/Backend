package com.example.importease.controller;

import com.example.importease.model.ImportItem;
import com.example.importease.service.ImportItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")

public class ImportItemController {

    @Autowired
    private ImportItemService service;

    @GetMapping
    public List<ImportItem> getAllItems() {
        return service.getAllItems();
    }

    @PostMapping
    public ImportItem addItem(@RequestBody ImportItem item) {
        return service.saveItem(item);
    }
}

package com.example.importease.controller;

import com.example.importease.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@CrossOrigin(origins = "*")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/signature")
    public ResponseEntity<?> getUploadSignature() {
        if (cloudinaryService.getCloudName() == null || cloudinaryService.getCloudName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cloudinary not configured"));
        }

        long timestamp = System.currentTimeMillis() / 1000;
        Map<String, Object> params = Map.of(
                "timestamp", timestamp,
                "upload_preset", "importease"
        );
        String signature = cloudinaryService.generateSignature(params);

        return ResponseEntity.ok(Map.of(
                "signature", signature,
                "timestamp", timestamp,
                "cloudName", cloudinaryService.getCloudName(),
                "apiKey", cloudinaryService.getApiKey(),
                "uploadPreset", "importease"
        ));
    }
}
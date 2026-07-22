package com.example.importease.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class CloudinaryService {

    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    private String cloudName;
    private String apiKey;
    private String apiSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    void parseUrl() {
        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            try {
                // Format: cloudinary://<api_key>:<api_secret>@<cloud_name>
                String withoutScheme = cloudinaryUrl.replace("cloudinary://", "");
                String[] atParts = withoutScheme.split("@");
                if (atParts.length == 2) {
                    String[] credentials = atParts[0].split(":");
                    if (credentials.length == 2) {
                        apiKey = credentials[0];
                        apiSecret = credentials[1];
                    }
                    cloudName = atParts[1];
                }
            } catch (Exception ex) {
                System.err.println("Failed to parse CLOUDINARY_URL: " + ex.getMessage());
            }
        }
    }

    public String getCloudName() {
        return cloudName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isConfigured() {
        return cloudName != null && !cloudName.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank();
    }

    public String generateSignature(Map<String, Object> paramsToSign) {
        TreeMap<String, Object> sorted = new TreeMap<>(paramsToSign);
        String toSign = sorted.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"))
                + apiSecret;
        return sha1Hex(toSign);
    }

    public String uploadFile(org.springframework.web.multipart.MultipartFile file) throws IOException {
        if (!isConfigured()) {
            throw new RuntimeException("Cloudinary not configured");
        }

        long timestamp = System.currentTimeMillis() / 1000;
        Map<String, Object> params = new TreeMap<>();
        params.put("timestamp", timestamp);

        String signature = generateSignature(params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("api_key", apiKey);
        body.add("timestamp", String.valueOf(timestamp));
        body.add("signature", signature);

        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
        HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getBody() != null && response.getBody().get("secure_url") != null) {
            return (String) response.getBody().get("secure_url");
        }
        throw new IOException("Cloudinary upload failed: " + (response.getBody() != null ? response.getBody() : "no response"));
    }

    public boolean deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) return false;
        if (!isConfigured()) return false;

        long timestamp = System.currentTimeMillis() / 1000;
        Map<String, Object> params = new TreeMap<>();
        params.put("public_id", publicId);
        params.put("timestamp", timestamp);
        String signature = generateSignature(params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("public_id", publicId);
        body.add("api_key", apiKey);
        body.add("timestamp", String.valueOf(timestamp));
        body.add("signature", signature);

        try {
            String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy";
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return "ok".equals(response.getBody() != null ? response.getBody().get("result") : null);
        } catch (Exception ex) {
            System.err.println("Cloudinary delete failed: " + ex.getMessage());
            return false;
        }
    }

    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return null;
        if (!imageUrl.contains("cloudinary.com")) return null;

        try {
            // URL format: https://res.cloudinary.com/{cloud}/image/upload/v{version}/{public_id}.{ext}
            // Or: https://res.cloudinary.com/{cloud}/image/upload/{public_id}.{ext}
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) return null;
            String afterUpload = parts[1];
            // Remove version prefix like v12345/
            if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }
            // Remove extension
            if (afterUpload.contains(".")) {
                afterUpload = afterUpload.substring(0, afterUpload.lastIndexOf("."));
            }
            return afterUpload;
        } catch (Exception ex) {
            System.err.println("Failed to extract public_id: " + ex.getMessage());
            return null;
        }
    }

    private String sha1Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("SHA-1 not available", ex);
        }
    }
}
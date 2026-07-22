package com.example.importease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CloudinaryServiceTest {

    private CloudinaryService service;

    @BeforeEach
    void setUp() {
        service = new CloudinaryService();
        ReflectionTestUtils.setField(service, "cloudinaryUrl", "cloudinary://testkey:testsecret@testcloud");
        service.parseUrl();
    }

    @Test
    void generateSignatureIsDeterministic() {
        Map<String, Object> params = Map.of("timestamp", 12345L, "upload_preset", "importease");
        String sig1 = service.generateSignature(params);
        String sig2 = service.generateSignature(params);
        assertEquals(sig1, sig2, "Signature should be deterministic for same params");
    }

    @Test
    void generateSignatureProducesValidHex() {
        Map<String, Object> params = Map.of("timestamp", 1000L);
        String sig = service.generateSignature(params);
        assertNotNull(sig);
        assertEquals(40, sig.length(), "SHA-1 hex should be 40 characters");
        assertTrue(sig.matches("^[a-f0-9]{40}$"), "Signature should be lowercase hex");
    }

    @Test
    void generateSignatureChangesWhenParamsChange() {
        String sig1 = service.generateSignature(Map.of("timestamp", 1L));
        String sig2 = service.generateSignature(Map.of("timestamp", 2L));
        assertNotEquals(sig1, sig2, "Different timestamps should produce different signatures");
    }

    @Test
    void extractPublicIdFromStandardUrl() {
        String url = "https://res.cloudinary.com/testcloud/image/upload/v12345/myproduct.jpg";
        assertEquals("myproduct", service.extractPublicId(url));
    }

    @Test
    void extractPublicIdFromUrlWithoutVersion() {
        String url = "https://res.cloudinary.com/testcloud/image/upload/myproduct.jpg";
        assertEquals("myproduct", service.extractPublicId(url));
    }

    @Test
    void extractPublicIdFromUrlWithFolder() {
        String url = "https://res.cloudinary.com/testcloud/image/upload/v12345/products/myproduct.jpg";
        assertEquals("products/myproduct", service.extractPublicId(url));
    }

    @Test
    void extractPublicIdReturnsNullForNonCloudinaryUrl() {
        assertNull(service.extractPublicId("https://example.com/image.jpg"));
    }

    @Test
    void extractPublicIdReturnsNullForNullInput() {
        assertNull(service.extractPublicId(null));
    }

    @Test
    void extractPublicIdReturnsNullForBlankInput() {
        assertNull(service.extractPublicId(""));
    }

    @Test
    void deleteImageReturnsFalseWhenNotConfigured() {
        service = new CloudinaryService();
        assertFalse(service.deleteImage("testid"));
    }

    @Test
    void deleteImageReturnsFalseForNullPublicId() {
        assertFalse(service.deleteImage(null));
    }

    @Test
    void deleteImageReturnsFalseForBlankPublicId() {
        assertFalse(service.deleteImage(""));
    }
}
package com.example.importease.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesProductWithSupplierWithoutStackOverflow() {
        Supplier supplier = new Supplier("Test Supplier", "test@test.com", "123456789", "Accra", "Tema");
        Product product = new Product("Test Product", "A test product", new BigDecimal("99.99"), 10, "http://example.com/img.jpg", supplier);
        supplier.setProducts(List.of(product));

        assertDoesNotThrow(() -> objectMapper.writeValueAsString(product));
    }

    @Test
    void serializedJsonContainsPriceField() throws Exception {
        Supplier supplier = new Supplier("Test Supplier", "test@test.com", "123456789", "Accra", "Tema");
        Product product = new Product("Test Product", "A test product", new BigDecimal("99.99"), 10, "http://example.com/img.jpg", supplier);
        supplier.setProducts(List.of(product));

        String json = objectMapper.writeValueAsString(product);

        assertNotNull(json);
        assertTrue(json.contains("\"price\""));
    }

    @Test
    void serializedJsonContainsSupplierName() throws Exception {
        Supplier supplier = new Supplier("Test Supplier", "test@test.com", "123456789", "Accra", "Tema");
        Product product = new Product("Test Product", "A test product", new BigDecimal("99.99"), 10, "http://example.com/img.jpg", supplier);
        supplier.setProducts(List.of(product));

        String json = objectMapper.writeValueAsString(product);

        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"Test Supplier\""));
    }

    @Test
    void serializedJsonDoesNotContainSupplierProducts() throws Exception {
        Supplier supplier = new Supplier("Test Supplier", "test@test.com", "123456789", "Accra", "Tema");
        Product product = new Product("Test Product", "A test product", new BigDecimal("99.99"), 10, "http://example.com/img.jpg", supplier);
        supplier.setProducts(List.of(product));

        String json = objectMapper.writeValueAsString(product);

        assertNotNull(json);
        assertFalse(json.contains("\"products\""), "JSON should not contain 'products' field to avoid circular reference");
    }

    @Test
    void serializedJsonContainsProductDetailFields() throws Exception {
        Supplier supplier = new Supplier("Test Supplier", "test@test.com", "123456789", "Accra", "Tema");
        Product product = new Product("Test Product", "A test product", new BigDecimal("99.99"), 10, "http://example.com/img.jpg", supplier);
        supplier.setProducts(List.of(product));

        String json = objectMapper.writeValueAsString(product);

        assertTrue(json.contains("\"description\":\"A test product\""));
        assertTrue(json.contains("\"imageUrl\":\"http://example.com/img.jpg\""));
        assertTrue(json.contains("\"quantity\":10"));
    }

    @Test
    void serializedJsonSupplierDoesNotContainNestedProducts() throws Exception {
        Supplier supplier = new Supplier("Test Supplier", "test@test.com", "123456789", "Accra", "Tema");
        Product product = new Product("Test Product", "A test product", new BigDecimal("99.99"), 10, "http://example.com/img.jpg", supplier);
        supplier.setProducts(List.of(product));

        String json = objectMapper.writeValueAsString(product);

        assertTrue(json.contains("\"supplier\""));

        int supplierFieldStart = json.indexOf("\"supplier\"");
        String supplierSection = json.substring(supplierFieldStart);

        assertFalse(supplierSection.contains("\"products\""),
                "The supplier section of the JSON should not contain a 'products' field");
    }
}

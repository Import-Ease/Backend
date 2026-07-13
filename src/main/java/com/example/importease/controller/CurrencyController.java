package com.example.importease.controller;

import com.example.importease.service.CurrencyConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
@CrossOrigin(origins = "*")
public class CurrencyController {

    private final CurrencyConversionService currencyConversionService;

    public CurrencyController(CurrencyConversionService currencyConversionService) {
        this.currencyConversionService = currencyConversionService;
    }

    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convert(
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "GHS") String from,
            @RequestParam(defaultValue = "USD") String to) {

        try {
            BigDecimal converted = currencyConversionService.convert(amount, from, to);
            return ResponseEntity.ok(Map.of(
                    "from", from.toUpperCase(),
                    "to", to.toUpperCase(),
                    "originalAmount", amount,
                    "convertedAmount", converted
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
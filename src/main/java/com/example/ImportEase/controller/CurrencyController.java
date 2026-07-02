package com.example.importease.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
@CrossOrigin(origins = "*")
public class CurrencyController {

    // Fixed demo rates: 1 GHS = X units of target currency
    private static final Map<String, Double> RATES_FROM_GHS = Map.of(
            "USD", 0.066,
            "EUR", 0.061,
            "GBP", 0.052,
            "CNY", 0.48,
            "GHS", 1.0
    );

    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convert(
            @RequestParam Double amount,
            @RequestParam(defaultValue = "GHS") String from,
            @RequestParam(defaultValue = "USD") String to) {

        Double fromRate = RATES_FROM_GHS.get(from.toUpperCase());
        Double toRate = RATES_FROM_GHS.get(to.toUpperCase());

        if (fromRate == null || toRate == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unsupported currency"));
        }

        double amountInGhs = amount / fromRate;
        double converted = amountInGhs * toRate;

        return ResponseEntity.ok(Map.of(
                "from", from.toUpperCase(),
                "to", to.toUpperCase(),
                "originalAmount", amount,
                "convertedAmount", Math.round(converted * 100.0) / 100.0
        ));
    }
}
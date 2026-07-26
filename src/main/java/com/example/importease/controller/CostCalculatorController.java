package com.example.importease.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/calculator")
@CrossOrigin(origins = "*")
public class CostCalculatorController {

    @GetMapping
    public ResponseEntity<Map<String, Double>> calculateCost(
            @RequestParam String origin,
            @RequestParam String goodsType,
            @RequestParam Double weightKg,
            @RequestParam(defaultValue = "false") boolean insurance) {

        double shippingRatePerKg = switch (origin.toLowerCase()) {
            case "china" -> 2.95;
            case "india" -> 3.20;
            case "turkey" -> 3.80;
            case "europe" -> 4.50;
            default -> 3.50;
        };

        double dutyRate = switch (goodsType.toLowerCase()) {
            case "electronics" -> 0.20;
            case "cold chain" -> 0.15;
            case "general goods" -> 0.10;
            default -> 0.12;
        };

        double shipping = weightKg * shippingRatePerKg;
        double harbour = 200.0;
        double duties = shipping * dutyRate;
        double transport = 150.0;
        double insuranceCost = insurance ? (shipping + duties) * 0.02 : 0.0;
        double total = shipping + harbour + duties + transport + insuranceCost;

        return ResponseEntity.ok(Map.of(
                "shipping", Math.round(shipping * 100.0) / 100.0,
                "harbour", harbour,
                "duties", Math.round(duties * 100.0) / 100.0,
                "transport", transport,
                "insurance", Math.round(insuranceCost * 100.0) / 100.0,
                "total", Math.round(total * 100.0) / 100.0
        ));
    }
}
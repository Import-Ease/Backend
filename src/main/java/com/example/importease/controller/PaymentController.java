package com.example.importease.controller;

import com.example.importease.service.PaystackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaystackService paystackService;

    public PaymentController(PaystackService paystackService) {
        this.paystackService = paystackService;
    }

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initialize(@RequestBody Map<String, Object> request) {
        try {
            String payerEmail = String.valueOf(request.get("payerEmail"));
            String supplierName = String.valueOf(request.get("supplierName"));
            BigDecimal amount = new BigDecimal(String.valueOf(request.get("amount")));
            String currency = String.valueOf(request.getOrDefault("currency", "NGN"));

            Map<String, Object> result = paystackService.initializePayment(payerEmail, supplierName, amount, currency);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestParam String reference) {
        try {
            Map<String, Object> result = paystackService.verifyPayment(reference);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}

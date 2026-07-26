package com.example.importease.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class CurrencyConversionService {

    private static final Map<String, BigDecimal> RATES_FROM_GHS = Map.of(
            "USD", new BigDecimal("0.066"),
            "EUR", new BigDecimal("0.061"),
            "GBP", new BigDecimal("0.052"),
            "CNY", new BigDecimal("0.48"),
            "GHS", BigDecimal.ONE
    );

    public BigDecimal convert(BigDecimal amount, String from, String to) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        String fromCode = from == null ? "" : from.toUpperCase();
        String toCode = to == null ? "" : to.toUpperCase();

        BigDecimal fromRate = RATES_FROM_GHS.get(fromCode);
        BigDecimal toRate = RATES_FROM_GHS.get(toCode);

        if (fromRate == null || toRate == null) {
            throw new IllegalArgumentException("Unsupported currency");
        }

        BigDecimal amountInGhs = amount.divide(fromRate, 10, RoundingMode.HALF_UP);
        return amountInGhs.multiply(toRate).setScale(2, RoundingMode.HALF_UP);
    }
}

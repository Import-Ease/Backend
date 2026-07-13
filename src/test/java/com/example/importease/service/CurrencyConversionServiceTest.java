package com.example.importease.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyConversionServiceTest {

    private final CurrencyConversionService currencyConversionService = new CurrencyConversionService();

    @Test
    void convertsUsingPreciseDecimalMath() {
        BigDecimal converted = currencyConversionService.convert(new BigDecimal("100"), "USD", "GHS");
        assertEquals(new BigDecimal("1515.15"), converted.setScale(2));
    }

    @Test
    void rejectsZeroOrNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> currencyConversionService.convert(BigDecimal.ZERO, "USD", "GHS"));
        assertThrows(IllegalArgumentException.class, () -> currencyConversionService.convert(new BigDecimal("-1"), "USD", "GHS"));
    }
}

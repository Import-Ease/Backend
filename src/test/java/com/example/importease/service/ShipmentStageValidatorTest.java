package com.example.importease.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentStageValidatorTest {

    @Test
    void validTransitionPasses() {
        assertDoesNotThrow(() ->
                ShipmentStageValidator.validateTransition("ORDER_CREATED", "SUPPLIER_CONFIRMED"));
    }

    @Test
    void invalidTransitionThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ShipmentStageValidator.validateTransition("ORDER_CREATED", "DELIVERED"));
        assertTrue(ex.getMessage().contains("ORDER_CREATED"));
        assertTrue(ex.getMessage().contains("DELIVERED"));
    }

    @Test
    void transitionFromTerminalThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ShipmentStageValidator.validateTransition("DELIVERED", "ORDER_CREATED"));
        assertTrue(ex.getMessage().contains("terminal"));
    }

    @Test
    void unknownStatusThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ShipmentStageValidator.validateTransition("UNKNOWN", "DELIVERED"));
    }
}

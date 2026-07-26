package com.example.importease.service;

import com.example.importease.model.ShipmentStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ShipmentWorkflowTest {

    @Test
    void validTransitionsSucceed() {
        assertTrue(ShipmentWorkflow.isValidTransition(ShipmentStatus.ORDER_CREATED, ShipmentStatus.SUPPLIER_CONFIRMED));
        assertTrue(ShipmentWorkflow.isValidTransition(ShipmentStatus.SUPPLIER_CONFIRMED, ShipmentStatus.SUPPLIER_PAID));
        assertTrue(ShipmentWorkflow.isValidTransition(ShipmentStatus.SUPPLIER_PAID, ShipmentStatus.AWAITING_PICKUP));
        assertTrue(ShipmentWorkflow.isValidTransition(ShipmentStatus.AWAITING_PICKUP, ShipmentStatus.COLLECTED));
        assertTrue(ShipmentWorkflow.isValidTransition(ShipmentStatus.COLLECTED, ShipmentStatus.ORIGIN_WAREHOUSE));
        assertTrue(ShipmentWorkflow.isValidTransition(ShipmentStatus.ORIGIN_WAREHOUSE, ShipmentStatus.EXPORT_CUSTOMS));
        assertTrue(ShipmentWorkflow.isValidTransition(ShipmentStatus.EXPORT_CUSTOMS, ShipmentStatus.IN_TRANSIT));
        assertTrue(ShipmentWorkflow.isValidTransition(ShipmentStatus.IN_TRANSIT, ShipmentStatus.DESTINATION_PORT));
        assertTrue(ShipmentWorkflow.isValidTransition(ShipmentStatus.DESTINATION_PORT, ShipmentStatus.IMPORT_CUSTOMS));
        assertTrue(ShipmentWorkflow.isValidTransition(ShipmentStatus.IMPORT_CUSTOMS, ShipmentStatus.WAREHOUSE));
        assertTrue(ShipmentWorkflow.isValidTransition(ShipmentStatus.WAREHOUSE, ShipmentStatus.OUT_FOR_DELIVERY));
        assertTrue(ShipmentWorkflow.isValidTransition(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED));
    }

    @Test
    void invalidTransitionFails() {
        assertFalse(ShipmentWorkflow.isValidTransition(ShipmentStatus.ORDER_CREATED, ShipmentStatus.DELIVERED));
        assertFalse(ShipmentWorkflow.isValidTransition(ShipmentStatus.IN_TRANSIT, ShipmentStatus.ORDER_CREATED));
        assertFalse(ShipmentWorkflow.isValidTransition(ShipmentStatus.DELIVERED, ShipmentStatus.ORDER_CREATED));
    }

    @Test
    void cannotTransitionFromTerminalStatus() {
        assertTrue(ShipmentWorkflow.isTerminal(ShipmentStatus.DELIVERED));
        assertTrue(ShipmentWorkflow.isTerminal(ShipmentStatus.ARCHIVED));
    }

    @Test
    void orderingIncludesAllStages() {
        assertEquals(13, ShipmentWorkflow.getOrderedStages().size());
        assertEquals(ShipmentStatus.ORDER_CREATED, ShipmentWorkflow.getOrderedStages().get(0));
        assertEquals(ShipmentStatus.DELIVERED, ShipmentWorkflow.getOrderedStages().get(12));
    }

    @Test
    void allowedTransitionsReturnsExpected() {
        assertEquals(Set.of(ShipmentStatus.SUPPLIER_CONFIRMED),
                ShipmentWorkflow.getAllowedTransitions(ShipmentStatus.ORDER_CREATED));
    }

    @Test
    void terminalStatusHasNoAllowedTransitions() {
        assertTrue(ShipmentWorkflow.getAllowedTransitions(ShipmentStatus.DELIVERED).isEmpty());
    }

    @Test
    void getErrorMessageIsInformative() {
        String msg = ShipmentWorkflow.getErrorMessage(ShipmentStatus.ORDER_CREATED, ShipmentStatus.DELIVERED);
        assertTrue(msg.contains("ORDER_CREATED"));
        assertTrue(msg.contains("DELIVERED"));
    }
}

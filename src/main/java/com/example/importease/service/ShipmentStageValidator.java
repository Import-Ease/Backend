package com.example.importease.service;

import com.example.importease.model.ShipmentStatus;

public final class ShipmentStageValidator {

    public static void validateTransition(String currentStatus, String nextStatus) {
        ShipmentStatus from;
        ShipmentStatus to;
        try {
            from = ShipmentStatus.valueOf(currentStatus);
            to = ShipmentStatus.valueOf(nextStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status: " + e.getMessage());
        }

        if (ShipmentWorkflow.isTerminal(from)) {
            throw new IllegalArgumentException(
                "Cannot transition from terminal status " + from
            );
        }

        if (!ShipmentWorkflow.isValidTransition(from, to)) {
            throw new IllegalArgumentException(ShipmentWorkflow.getErrorMessage(from, to));
        }
    }

    private ShipmentStageValidator() {}
}

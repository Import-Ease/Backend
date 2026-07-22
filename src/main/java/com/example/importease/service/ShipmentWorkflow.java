package com.example.importease.service;

import com.example.importease.model.ShipmentStatus;
import java.util.*;

public final class ShipmentWorkflow {

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> TRANSITIONS = new HashMap<>();
    private static final List<ShipmentStatus> ORDERED_STAGES = List.of(
        ShipmentStatus.ORDER_CREATED,
        ShipmentStatus.SUPPLIER_CONFIRMED,
        ShipmentStatus.SUPPLIER_PAID,
        ShipmentStatus.AWAITING_PICKUP,
        ShipmentStatus.COLLECTED,
        ShipmentStatus.ORIGIN_WAREHOUSE,
        ShipmentStatus.EXPORT_CUSTOMS,
        ShipmentStatus.IN_TRANSIT,
        ShipmentStatus.DESTINATION_PORT,
        ShipmentStatus.IMPORT_CUSTOMS,
        ShipmentStatus.WAREHOUSE,
        ShipmentStatus.OUT_FOR_DELIVERY,
        ShipmentStatus.DELIVERED
    );
    private static final Set<ShipmentStatus> TERMINAL_STATES = Set.of(
        ShipmentStatus.DELIVERED,
        ShipmentStatus.ARCHIVED
    );

    static {
        TRANSITIONS.put(ShipmentStatus.ORDER_CREATED, Set.of(ShipmentStatus.SUPPLIER_CONFIRMED));
        TRANSITIONS.put(ShipmentStatus.SUPPLIER_CONFIRMED, Set.of(ShipmentStatus.SUPPLIER_PAID));
        TRANSITIONS.put(ShipmentStatus.SUPPLIER_PAID, Set.of(ShipmentStatus.AWAITING_PICKUP));
        TRANSITIONS.put(ShipmentStatus.AWAITING_PICKUP, Set.of(ShipmentStatus.COLLECTED));
        TRANSITIONS.put(ShipmentStatus.COLLECTED, Set.of(ShipmentStatus.ORIGIN_WAREHOUSE));
        TRANSITIONS.put(ShipmentStatus.ORIGIN_WAREHOUSE, Set.of(ShipmentStatus.EXPORT_CUSTOMS));
        TRANSITIONS.put(ShipmentStatus.EXPORT_CUSTOMS, Set.of(ShipmentStatus.IN_TRANSIT));
        TRANSITIONS.put(ShipmentStatus.IN_TRANSIT, Set.of(ShipmentStatus.DESTINATION_PORT));
        TRANSITIONS.put(ShipmentStatus.DESTINATION_PORT, Set.of(ShipmentStatus.IMPORT_CUSTOMS));
        TRANSITIONS.put(ShipmentStatus.IMPORT_CUSTOMS, Set.of(ShipmentStatus.WAREHOUSE));
        TRANSITIONS.put(ShipmentStatus.WAREHOUSE, Set.of(ShipmentStatus.OUT_FOR_DELIVERY));
        TRANSITIONS.put(ShipmentStatus.OUT_FOR_DELIVERY, Set.of(ShipmentStatus.DELIVERED));
        TRANSITIONS.put(ShipmentStatus.DELIVERED, Set.of());

        mapLegacyStatus(ShipmentStatus.PENDING, ShipmentStatus.ORDER_CREATED);
        mapLegacyStatus(ShipmentStatus.PENDING_PAYMENT, ShipmentStatus.ORDER_CREATED);
        mapLegacyStatus(ShipmentStatus.ORIGIN, ShipmentStatus.ORIGIN_WAREHOUSE);
        mapLegacyStatus(ShipmentStatus.TRANSIT, ShipmentStatus.IN_TRANSIT);
        mapLegacyStatus(ShipmentStatus.AT_PORT, ShipmentStatus.DESTINATION_PORT);
        mapLegacyStatus(ShipmentStatus.CUSTOMS, ShipmentStatus.IMPORT_CUSTOMS);
        mapLegacyStatus(ShipmentStatus.ARCHIVED, ShipmentStatus.DELIVERED);
    }

    private static void mapLegacyStatus(ShipmentStatus legacy, ShipmentStatus modern) {
        int idx = ORDERED_STAGES.indexOf(modern);
        Set<ShipmentStatus> next = new HashSet<>();
        if (idx >= 0 && idx < ORDERED_STAGES.size() - 1) {
            next.add(ORDERED_STAGES.get(idx + 1));
        }
        TRANSITIONS.put(legacy, next);
    }

    public static boolean isValidTransition(ShipmentStatus from, ShipmentStatus to) {
        Set<ShipmentStatus> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public static String getErrorMessage(ShipmentStatus from, ShipmentStatus to) {
        return String.format("Invalid transition from %s to %s", from, to);
    }

    public static List<ShipmentStatus> getOrderedStages() {
        return ORDERED_STAGES;
    }

    public static boolean isTerminal(ShipmentStatus status) {
        return TERMINAL_STATES.contains(status);
    }

    public static Set<ShipmentStatus> getAllowedTransitions(ShipmentStatus from) {
        return TRANSITIONS.getOrDefault(from, Set.of());
    }

    private ShipmentWorkflow() {}
}

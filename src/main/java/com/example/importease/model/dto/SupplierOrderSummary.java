package com.example.importease.model.dto;

import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentStatus;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SupplierOrderSummary {

    // Statuses that represent "not yet shipped / awaiting action"
    private static final Set<ShipmentStatus> PENDING_STATUSES = Set.of(
            ShipmentStatus.ORDER_CREATED,
            ShipmentStatus.SUPPLIER_CONFIRMED,
            ShipmentStatus.SUPPLIER_PAID,
            ShipmentStatus.AWAITING_PICKUP,
            ShipmentStatus.PENDING,
            ShipmentStatus.PENDING_PAYMENT
    );

    // Statuses that represent "sent / on the way, not yet delivered"
    private static final Set<ShipmentStatus> IN_TRANSIT_STATUSES = Set.of(
            ShipmentStatus.COLLECTED,
            ShipmentStatus.ORIGIN_WAREHOUSE,
            ShipmentStatus.EXPORT_CUSTOMS,
            ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.DESTINATION_PORT,
            ShipmentStatus.IMPORT_CUSTOMS,
            ShipmentStatus.WAREHOUSE,
            ShipmentStatus.OUT_FOR_DELIVERY,
            ShipmentStatus.ORIGIN,
            ShipmentStatus.TRANSIT,
            ShipmentStatus.AT_PORT,
            ShipmentStatus.CUSTOMS
    );

    private long totalOrders;
    private long distinctCustomers;
    private long pendingCount;
    private long inTransitCount;
    private long deliveredCount;
    private Map<String, Long> byStatus;

    public static SupplierOrderSummary from(List<Shipment> shipments) {
        SupplierOrderSummary summary = new SupplierOrderSummary();

        summary.totalOrders = shipments.size();

        Set<Object> customerIds = new HashSet<>();
        Map<ShipmentStatus, Long> counts = new EnumMap<>(ShipmentStatus.class);

        for (Shipment s : shipments) {
            if (s.getUser() != null) {
                customerIds.add(s.getUser().getId());
            }
            ShipmentStatus status = s.getStatus();
            counts.merge(status, 1L, Long::sum);

            if (PENDING_STATUSES.contains(status)) {
                summary.pendingCount++;
            } else if (IN_TRANSIT_STATUSES.contains(status)) {
                summary.inTransitCount++;
            } else if (status == ShipmentStatus.DELIVERED) {
                summary.deliveredCount++;
            }
        }

        summary.distinctCustomers = customerIds.size();

        Map<String, Long> byStatus = new java.util.LinkedHashMap<>();
        for (Map.Entry<ShipmentStatus, Long> entry : counts.entrySet()) {
            byStatus.put(entry.getKey().name(), entry.getValue());
        }
        summary.byStatus = byStatus;

        return summary;
    }

    public long getTotalOrders() { return totalOrders; }
    public long getDistinctCustomers() { return distinctCustomers; }
    public long getPendingCount() { return pendingCount; }
    public long getInTransitCount() { return inTransitCount; }
    public long getDeliveredCount() { return deliveredCount; }
    public Map<String, Long> getByStatus() { return byStatus; }
}
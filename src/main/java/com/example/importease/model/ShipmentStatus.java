package com.example.importease.model;

public enum ShipmentStatus {
    ORDER_CREATED,
    SUPPLIER_CONFIRMED,
    SUPPLIER_PAID,
    AWAITING_PICKUP,
    COLLECTED,
    ORIGIN_WAREHOUSE,
    EXPORT_CUSTOMS,
    IN_TRANSIT,
    DESTINATION_PORT,
    IMPORT_CUSTOMS,
    WAREHOUSE,
    OUT_FOR_DELIVERY,
    DELIVERED,

    @Deprecated PENDING,
    @Deprecated PENDING_PAYMENT,
    @Deprecated ORIGIN,
    @Deprecated TRANSIT,
    @Deprecated AT_PORT,
    @Deprecated CUSTOMS,
    @Deprecated ARCHIVED;
}

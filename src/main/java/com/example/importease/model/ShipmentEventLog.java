package com.example.importease.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipment_event_logs")
public class ShipmentEventLog {

    public enum EventType {
        STAGE_ADVANCED,
        DOCUMENT_UPLOADED,
        CHECKPOINT_ADDED,
        SHIPMENT_UPDATED,
        SHIPMENT_CREATED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private String performedBy;

    public ShipmentEventLog() {}

    public ShipmentEventLog(Shipment shipment, EventType eventType, String description, String performedBy) {
        this.shipment = shipment;
        this.eventType = eventType;
        this.description = description;
        this.performedBy = performedBy;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Shipment getShipment() { return shipment; }
    public EventType getEventType() { return eventType; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getPerformedBy() { return performedBy; }
}

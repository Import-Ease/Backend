package com.example.importease.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipment_checkpoints")
public class ShipmentCheckpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Shipment shipment;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ShipmentCheckpoint() {}

    public ShipmentCheckpoint(Shipment shipment, String location, String description, LocalDateTime timestamp) {
        this.shipment = shipment;
        this.location = location;
        this.description = description;
        this.timestamp = timestamp;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Shipment getShipment() { return shipment; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

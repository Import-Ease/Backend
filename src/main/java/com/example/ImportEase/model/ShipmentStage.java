package com.example.importease.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipment_stages")
public class ShipmentStage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(nullable = false)
    private String stageName;

    @Column(nullable = false)
    private LocalDateTime reachedAt = LocalDateTime.now();

    private String note;

    public ShipmentStage() {}

    public ShipmentStage(Shipment shipment, String stageName, String note) {
        this.shipment = shipment;
        this.stageName = stageName;
        this.note = note;
        this.reachedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public LocalDateTime getReachedAt() { return reachedAt; }
    public void setReachedAt(LocalDateTime reachedAt) { this.reachedAt = reachedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
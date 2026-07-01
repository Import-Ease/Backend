package com.example.importease.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "shipment_id")
    private UUID id;

    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingId;

    @Column(name = "description")
    private String description;

    @Column(name = "goods_type")
    private String goodsType;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "origin_port")
    private String originPort;

    @Column(name = "destination_port")
    private String destinationPort;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "estimated_arrival")
    private LocalDate estimatedTimeOfArrival;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "archived")
    private boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Shipment() {}

    public Shipment(String trackingId, String description, String goodsType,
                    String carrier, String originPort, String destinationPort,
                    Double weightKg, LocalDate estimatedTimeOfArrival, AppUser user) {
        this.trackingId = trackingId;
        this.description = description;
        this.goodsType = goodsType;
        this.carrier = carrier;
        this.originPort = originPort;
        this.destinationPort = destinationPort;
        this.weightKg = weightKg;
        this.estimatedTimeOfArrival = estimatedTimeOfArrival;
        this.user = user;
        this.status = "PENDING";
        this.archived = false;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGoodsType() { return goodsType; }
    public void setGoodsType(String goodsType) { this.goodsType = goodsType; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public String getOriginPort() { return originPort; }
    public void setOriginPort(String originPort) { this.originPort = originPort; }
    public String getDestinationPort() { return destinationPort; }
    public void setDestinationPort(String destinationPort) { this.destinationPort = destinationPort; }
    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public LocalDate getEstimatedTimeOfArrival() { return estimatedTimeOfArrival; }
    public void setEstimatedTimeOfArrival(LocalDate eta) { this.estimatedTimeOfArrival = eta; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
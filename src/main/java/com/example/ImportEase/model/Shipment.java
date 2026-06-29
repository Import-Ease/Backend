package com.example.ImportEase.model;

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

    @Transient
    private String description;

    @Transient
    private String goodsType;

    @Column(name = "shipping_method")
    private String carrier;

    @Column(name = "estimated_arrival")
    private LocalDate estimatedTimeOfArrival;

    @Column(name = "status")
    private String status = "PENDING";

    @Transient
    private boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Shipment() {}

    public Shipment(String trackingId, String description, String goodsType, String carrier, LocalDate estimatedTimeOfArrival, AppUser user) {
        this.trackingId = trackingId;
        this.description = description;
        this.goodsType = goodsType;
        this.carrier = carrier;
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

    public LocalDate getEstimatedTimeOfArrival() { return estimatedTimeOfArrival; }
    public void setEstimatedTimeOfArrival(LocalDate estimatedTimeOfArrival) { this.estimatedTimeOfArrival = estimatedTimeOfArrival; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
package com.example.ImportEase.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ShipmentRequest {

    @NotBlank(message = "Tracking ID is required")
    private String trackingId;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Goods type is required")
    private String goodsType;

    @NotBlank(message = "Carrier/Shipping Line is required")
    private String carrier;

    @NotNull(message = "Estimated Time of Arrival (ETA) is required")
    @FutureOrPresent(message = "ETA must be today or in the future")
    private LocalDate estimatedTimeOfArrival;

    // Constructors
    public ShipmentRequest() {}

    public ShipmentRequest(String trackingId, String description, String goodsType, String carrier, LocalDate estimatedTimeOfArrival) {
        this.trackingId = trackingId;
        this.description = description;
        this.goodsType = goodsType;
        this.carrier = carrier;
        this.estimatedTimeOfArrival = estimatedTimeOfArrival;
    }

    // Getters and Setters
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
}
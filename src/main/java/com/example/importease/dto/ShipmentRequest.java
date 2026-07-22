package com.example.importease.dto;

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

    @NotBlank(message = "Carrier is required")
    private String carrier;

    private String originPort;
    private String destinationPort;
    private Double weightKg;

    @NotNull(message = "ETA is required")
    @FutureOrPresent(message = "ETA must be today or in the future")
    private LocalDate estimatedTimeOfArrival;

    public ShipmentRequest() {}

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
}
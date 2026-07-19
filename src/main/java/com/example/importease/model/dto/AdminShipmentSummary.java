package com.example.importease.model.dto;

import com.example.importease.model.Shipment;
import java.time.LocalDate;
import java.util.UUID;

public class AdminShipmentSummary {
    private UUID id;
    private String trackingId;
    private String description;
    private String status;
    private String carrier;
    private String originPort;
    private String destinationPort;
    private LocalDate estimatedTimeOfArrival;
    private String userEmail;
    private String userFullName;

    public AdminShipmentSummary(Shipment s) {
        this.id = s.getId();
        this.trackingId = s.getTrackingId();
        this.description = s.getDescription();
        this.status = s.getStatus();
        this.carrier = s.getCarrier();
        this.originPort = s.getOriginPort();
        this.destinationPort = s.getDestinationPort();
        this.estimatedTimeOfArrival = s.getEstimatedTimeOfArrival();
        this.userEmail = s.getUser() != null ? s.getUser().getEmail() : null;
        this.userFullName = s.getUser() != null ? s.getUser().getFullName() : null;
    }

    public UUID getId() { return id; }
    public String getTrackingId() { return trackingId; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getCarrier() { return carrier; }
    public String getOriginPort() { return originPort; }
    public String getDestinationPort() { return destinationPort; }
    public LocalDate getEstimatedTimeOfArrival() { return estimatedTimeOfArrival; }
    public String getUserEmail() { return userEmail; }
    public String getUserFullName() { return userFullName; }
}
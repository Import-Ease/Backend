package com.example.importease.model.dto;

import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentPaymentStatus;
import java.math.BigDecimal;
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
    private BigDecimal quotationAmount;
    private String quotationCurrency;
    private ShipmentPaymentStatus paymentStatus;
    private BigDecimal amountPaid;

    public AdminShipmentSummary(Shipment s) {
        this.id = s.getId();
        this.trackingId = s.getTrackingId();
        this.description = s.getDescription();
        this.status = s.getStatus().name();
        this.carrier = s.getCarrier();
        this.originPort = s.getOriginPort();
        this.destinationPort = s.getDestinationPort();
        this.estimatedTimeOfArrival = s.getEstimatedTimeOfArrival();
        this.userEmail = s.getUser() != null ? s.getUser().getEmail() : null;
        this.userFullName = s.getUser() != null ? s.getUser().getFullName() : null;
        this.quotationAmount = s.getQuotationAmount();
        this.quotationCurrency = s.getQuotationCurrency();
        this.paymentStatus = s.getPaymentStatus();
        this.amountPaid = s.getAmountPaid();
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
    public BigDecimal getQuotationAmount() { return quotationAmount; }
    public String getQuotationCurrency() { return quotationCurrency; }
    public ShipmentPaymentStatus getPaymentStatus() { return paymentStatus; }
    public BigDecimal getAmountPaid() { return amountPaid; }
}
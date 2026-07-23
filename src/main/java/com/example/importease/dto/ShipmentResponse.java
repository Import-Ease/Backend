package com.example.importease.dto;

import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentPaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ShipmentResponse {
    private UUID id;
    private String trackingId;
    private String description;
    private String goodsType;
    private String carrier;
    private String originPort;
    private String destinationPort;
    private Double weightKg;
    private LocalDate estimatedTimeOfArrival;
    private String status;
    private Long productId;
    private String shippingMode;
    private Integer orderQuantity;
    private String paySupplier;
    private LocalDateTime createdAt;
    private boolean archived;
    private String userEmail;
    private String userFullName;
    private BigDecimal quotationAmount;
    private String quotationCurrency;
    private ShipmentPaymentStatus paymentStatus;
    private BigDecimal amountPaid;

    public static ShipmentResponse fromEntity(Shipment s) {
        ShipmentResponse r = new ShipmentResponse();
        r.id = s.getId();
        r.trackingId = s.getTrackingId();
        r.description = s.getDescription();
        r.goodsType = s.getGoodsType();
        r.carrier = s.getCarrier();
        r.originPort = s.getOriginPort();
        r.destinationPort = s.getDestinationPort();
        r.weightKg = s.getWeightKg();
        r.estimatedTimeOfArrival = s.getEstimatedTimeOfArrival();
        r.status = s.getStatus().name();
        r.productId = s.getProductId();
        r.shippingMode = s.getShippingMode();
        r.orderQuantity = s.getOrderQuantity();
        r.paySupplier = s.getPaySupplier();
        r.quotationAmount = s.getQuotationAmount();
        r.quotationCurrency = s.getQuotationCurrency();
        r.paymentStatus = s.getPaymentStatus();
        r.amountPaid = s.getAmountPaid();
        r.createdAt = s.getCreatedAt();
        r.archived = s.isArchived();
        return r;
    }

    public static ShipmentResponse fromEntityWithUser(Shipment s) {
        ShipmentResponse r = fromEntity(s);
        if (s.getUser() != null) {
            r.userEmail = s.getUser().getEmail();
            r.userFullName = s.getUser().getFullName();
        }
        return r;
    }

    public UUID getId() { return id; }
    public String getTrackingId() { return trackingId; }
    public String getDescription() { return description; }
    public String getGoodsType() { return goodsType; }
    public String getCarrier() { return carrier; }
    public String getOriginPort() { return originPort; }
    public String getDestinationPort() { return destinationPort; }
    public Double getWeightKg() { return weightKg; }
    public LocalDate getEstimatedTimeOfArrival() { return estimatedTimeOfArrival; }
    public String getStatus() { return status; }
    public Long getProductId() { return productId; }
    public String getShippingMode() { return shippingMode; }
    public Integer getOrderQuantity() { return orderQuantity; }
    public String getPaySupplier() { return paySupplier; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isArchived() { return archived; }
    public String getUserEmail() { return userEmail; }
    public String getUserFullName() { return userFullName; }
    public BigDecimal getQuotationAmount() { return quotationAmount; }
    public String getQuotationCurrency() { return quotationCurrency; }
    public ShipmentPaymentStatus getPaymentStatus() { return paymentStatus; }
    public BigDecimal getAmountPaid() { return amountPaid; }
}

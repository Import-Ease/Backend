package com.example.importease.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Shipping mode is required (AIR or SEA)")
    private String shippingMode;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public OrderRequest() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getShippingMode() { return shippingMode; }
    public void setShippingMode(String shippingMode) { this.shippingMode = shippingMode; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}

package com.example.importease.model;

public class SearchResponseDto {
    private Long productId;
    private String productName;
    private Double productPrice;
    private String supplierName;
    private String supplierContact;
    private String originCountry;
    private String imageUrl;

    public SearchResponseDto(Long productId, String productName, Double productPrice,
                             String supplierName, String supplierContact,
                             String originCountry, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.supplierName = supplierName;
        this.supplierContact = supplierContact;
        this.originCountry = originCountry;
        this.imageUrl = imageUrl;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Double getProductPrice() { return productPrice; }
    public void setProductPrice(Double productPrice) { this.productPrice = productPrice; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierContact() { return supplierContact; }
    public void setSupplierContact(String supplierContact) { this.supplierContact = supplierContact; }
    public String getOriginCountry() { return originCountry; }
    public void setOriginCountry(String originCountry) { this.originCountry = originCountry; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
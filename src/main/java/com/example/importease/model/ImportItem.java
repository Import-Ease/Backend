package com.example.importease.model;

import jakarta.persistence.*;

@Entity
@Table(name = "import_item")
public class ImportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private String countryOfOrigin;
    private String status;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public ImportItem() {}

    public Long getId() { return id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCountryOfOrigin() { return countryOfOrigin; }
    public void setCountryOfOrigin(String countryOfOrigin) { this.countryOfOrigin = countryOfOrigin; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
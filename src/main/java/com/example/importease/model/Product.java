package com.example.importease.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private int quantity;

    private String originCountry;

    private Integer freightDays;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    // Constructors
    public Product() {}

    public Product(String name, String description, BigDecimal price, int quantity, String originCountry, Integer freightDays, Supplier supplier) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.originCountry = originCountry;
        this.freightDays = freightDays;
        this.supplier = supplier;

    }

    // Getters and Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getOriginCountry() { return originCountry; }
    public void setOriginCountry(String originCountry) { this.originCountry = originCountry; }

    public Integer getFreightDays() { return freightDays; }
    public void setFreightDays(Integer freightDays) { this.freightDays = freightDays; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
}
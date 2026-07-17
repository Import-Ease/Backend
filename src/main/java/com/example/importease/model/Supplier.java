package com.example.importease.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String phone;

    private String address;

    private String shippingOrigin;

    @Column(name = "owner_id")
    private UUID ownerId;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<Product> products;

    // Constructors
    public Supplier() {}

    public Supplier(String name, String email, String phone, String address, String shippingOrigin) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.shippingOrigin = shippingOrigin;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getShippingOrigin() { return shippingOrigin; }
    public void setShippingOrigin(String shippingOrigin) { this.shippingOrigin = shippingOrigin; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
}
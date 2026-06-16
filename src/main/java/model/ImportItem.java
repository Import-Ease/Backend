package com.example.importease.model;

import jakarta.persistence.*;

@Entity

public class ImportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private String countryOfOrigin;
    private String status;

    // getters and setters

}

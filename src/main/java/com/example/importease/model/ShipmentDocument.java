package com.example.importease.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipment_documents")
public class ShipmentDocument {

    public enum DocumentType {
        INVOICE,
        BILL_OF_LADING,
        PACKING_LIST,
        CERTIFICATE_OF_ORIGIN,
        CUSTOMS_DECLARATION,
        INSURANCE,
        OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    private String uploadedBy;

    public ShipmentDocument() {}

    public ShipmentDocument(Shipment shipment, DocumentType documentType, String fileName, String fileUrl, Long fileSize, String uploadedBy) {
        this.shipment = shipment;
        this.documentType = documentType;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.uploadedAt = LocalDateTime.now();
        this.uploadedBy = uploadedBy;
    }

    public UUID getId() { return id; }
    public Shipment getShipment() { return shipment; }
    public DocumentType getDocumentType() { return documentType; }
    public String getFileName() { return fileName; }
    public String getFileUrl() { return fileUrl; }
    public Long getFileSize() { return fileSize; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public String getUploadedBy() { return uploadedBy; }
}

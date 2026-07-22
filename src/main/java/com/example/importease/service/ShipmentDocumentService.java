package com.example.importease.service;

import com.example.importease.config.LoggingFilter;
import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentDocument;
import com.example.importease.model.ShipmentDocument.DocumentType;
import com.example.importease.model.ShipmentEventLog;
import com.example.importease.repository.ShipmentDocumentRepository;
import com.example.importease.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ShipmentDocumentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentDocumentService.class);

    private static final java.util.Set<String> ALLOWED_MIME_TYPES = java.util.Set.of(
            "application/pdf",
            "image/jpeg", "image/jpg", "image/png",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final ShipmentRepository shipmentRepository;
    private final ShipmentDocumentRepository documentRepository;
    private final CloudinaryService cloudinaryService;
    private final ShipmentEventLogService eventLogService;

    public ShipmentDocumentService(ShipmentRepository shipmentRepository,
                                   ShipmentDocumentRepository documentRepository,
                                   CloudinaryService cloudinaryService,
                                   ShipmentEventLogService eventLogService) {
        this.shipmentRepository = shipmentRepository;
        this.documentRepository = documentRepository;
        this.cloudinaryService = cloudinaryService;
        this.eventLogService = eventLogService;
    }

    @Transactional
    public ShipmentDocument uploadDocument(UUID shipmentId, String documentTypeStr,
                                           MultipartFile file, String uploadedBy) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));

        DocumentType documentType;
        try {
            documentType = DocumentType.valueOf(documentTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid document type: " + documentTypeStr);
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > 10_485_760L) {
            throw new IllegalArgumentException("File exceeds maximum size of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unnamed";
        }
        originalFilename = originalFilename.replaceAll("[/\\\\:<>\"|?*]", "_");

        String fileUrl;
        try {
            fileUrl = cloudinaryService.uploadFile(file);
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {} | correlationId={}",
                    e.getMessage(), LoggingFilter.correlationId());
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }

        ShipmentDocument doc = new ShipmentDocument(
                shipment, documentType, originalFilename, fileUrl, file.getSize(), uploadedBy
        );
        doc = documentRepository.save(doc);

        eventLogService.logEvent(shipmentId, ShipmentEventLog.EventType.DOCUMENT_UPLOADED,
                documentType + " uploaded: " + originalFilename, uploadedBy);

        log.info("Document uploaded: shipmentId={} | type={} | file={} | correlationId={}",
                shipmentId, documentType, originalFilename, LoggingFilter.correlationId());
        return doc;
    }

    public List<ShipmentDocument> getDocuments(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));
        return documentRepository.findByShipmentOrderByUploadedAtDesc(shipment);
    }
}

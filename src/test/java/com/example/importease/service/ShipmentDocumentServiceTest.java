package com.example.importease.service;

import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentDocument;
import com.example.importease.model.ShipmentDocument.DocumentType;
import com.example.importease.repository.ShipmentDocumentRepository;
import com.example.importease.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentDocumentServiceTest {

    @Mock ShipmentRepository shipmentRepository;
    @Mock ShipmentDocumentRepository documentRepository;
    @Mock CloudinaryService cloudinaryService;
    @Mock ShipmentEventLogService eventLogService;
    @Mock MultipartFile file;

    ShipmentDocumentService service;
    Shipment shipment;
    UUID shipmentId;

    @BeforeEach
    void setUp() {
        service = new ShipmentDocumentService(shipmentRepository, documentRepository, cloudinaryService, eventLogService);
        shipmentId = UUID.randomUUID();
        shipment = new Shipment();
        shipment.setId(shipmentId);
    }

    @Test
    void uploadDocument_savesAndReturnsDocument() throws IOException {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("invoice.pdf");
        when(file.getSize()).thenReturn(1024L);
        when(cloudinaryService.uploadFile(file)).thenReturn("https://res.cloudinary.com/demo/image/upload/v1/invoice.pdf");
        when(documentRepository.save(any(ShipmentDocument.class))).thenAnswer(i -> i.getArgument(0));

        ShipmentDocument result = service.uploadDocument(shipmentId, "INVOICE", file, "admin@example.com");

        assertEquals(DocumentType.INVOICE, result.getDocumentType());
        assertEquals("invoice.pdf", result.getFileName());
        assertEquals("https://res.cloudinary.com/demo/image/upload/v1/invoice.pdf", result.getFileUrl());
        assertEquals(1024L, result.getFileSize());
        assertEquals("admin@example.com", result.getUploadedBy());
        verify(documentRepository).save(any(ShipmentDocument.class));
    }

    @Test
    void uploadDocument_unknownType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.uploadDocument(shipmentId, "UNKNOWN_TYPE", file, "admin"));
    }

    @Test
    void uploadDocument_emptyFile_throws() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(file.isEmpty()).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> service.uploadDocument(shipmentId, "INVOICE", file, "admin"));
    }

    @Test
    void uploadDocument_shipmentNotFound_throws() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.uploadDocument(shipmentId, "INVOICE", file, "admin"));
    }

    @Test
    void getDocuments_returnsList() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(documentRepository.findByShipmentOrderByUploadedAtDesc(shipment))
                .thenReturn(List.of(new ShipmentDocument()));

        List<ShipmentDocument> docs = service.getDocuments(shipmentId);
        assertEquals(1, docs.size());
    }
}

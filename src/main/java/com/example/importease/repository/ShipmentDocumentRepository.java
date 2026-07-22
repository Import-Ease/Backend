package com.example.importease.repository;

import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShipmentDocumentRepository extends JpaRepository<ShipmentDocument, UUID> {
    List<ShipmentDocument> findByShipmentOrderByUploadedAtDesc(Shipment shipment);
}

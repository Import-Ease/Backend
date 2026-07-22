package com.example.importease.repository;

import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShipmentEventLogRepository extends JpaRepository<ShipmentEventLog, UUID> {
    List<ShipmentEventLog> findByShipmentOrderByCreatedAtDesc(Shipment shipment);
}

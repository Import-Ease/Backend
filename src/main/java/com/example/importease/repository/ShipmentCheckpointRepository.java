package com.example.importease.repository;

import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShipmentCheckpointRepository extends JpaRepository<ShipmentCheckpoint, UUID> {
    List<ShipmentCheckpoint> findByShipmentOrderByTimestampAsc(Shipment shipment);
}

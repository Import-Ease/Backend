package com.example.importease.repository;

import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShipmentStageRepository extends JpaRepository<ShipmentStage, java.util.UUID> {
    List<ShipmentStage> findByShipmentOrderByReachedAtAsc(Shipment shipment);

}
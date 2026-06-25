package com.example.ImportEase.repositories;

import com.example.ImportEase.models.Shipment;
import com.example.ImportEase.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    List<Shipment> findByUser(AppUser user);

    Optional<Shipment> findByIdAndUser(UUID id, AppUser user);

    boolean existsByTrackingId(String trackingId);
}

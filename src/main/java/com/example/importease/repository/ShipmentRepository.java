package com.example.importease.repository;

import com.example.importease.model.Shipment;
import com.example.importease.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    List<Shipment> findByUser(AppUser user);

    List<Shipment> findByUserAndArchivedFalse(AppUser user);

    Optional<Shipment> findByIdAndUser(UUID id, AppUser user);

    boolean existsByTrackingId(String trackingId);

    @Query("SELECT s FROM Shipment s JOIN FETCH s.user")
    List<Shipment> findAllWithUser();
}

package com.example.importease.service;

import com.example.importease.dto.ShipmentRequest;
import com.example.importease.model.AppUser;
import com.example.importease.model.Shipment;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.repository.ShipmentRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.importease.model.ShipmentStage;
import com.example.importease.repository.ShipmentStageRepository;
import java.util.List;
import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final AppUserRepository userRepository;

    private final ShipmentStageRepository shipmentStageRepository;

    // Update constructor to include it:
    public ShipmentService(ShipmentRepository shipmentRepository, AppUserRepository userRepository,
                           ShipmentStageRepository shipmentStageRepository) {
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.shipmentStageRepository = shipmentStageRepository;
    }

    // Admin manually advances a shipment to a new stage
    @Transactional
    public Shipment advanceStage(UUID shipmentId, String stageName, String note) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));

        ShipmentStage stage = new ShipmentStage(shipment, stageName, note);
        shipmentStageRepository.save(stage);

        shipment.setStatus(stageName);
        return shipmentRepository.save(shipment);
    }

    public List<ShipmentStage> getStageHistory(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));
        return shipmentStageRepository.findByShipmentOrderByReachedAtAsc(shipment);
    }

        @Transactional
    public Shipment createShipment(ShipmentRequest request, String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        if (shipmentRepository.existsByTrackingId(request.getTrackingId())) {
            throw new IllegalArgumentException("A shipment with this tracking ID already exists.");
        }

        Shipment shipment = new Shipment(
                request.getTrackingId(),
                request.getDescription(),
                request.getGoodsType(),
                request.getCarrier(),
                request.getOriginPort(),
                request.getDestinationPort(),
                request.getWeightKg(),
                request.getEstimatedTimeOfArrival(),
                user
        );

        return shipmentRepository.save(shipment);
    }

    public List<Shipment> getActiveShipments(String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));
        return shipmentRepository.findByUser(user).stream()
                .filter(s -> !s.isArchived())
                .toList();
    }

    public Shipment getShipmentById(UUID id, String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return shipmentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found or unauthorized."));
    }

    @Transactional
    public Shipment updateShipment(UUID id, ShipmentRequest request, String userEmail) {
        Shipment shipment = getShipmentById(id, userEmail);
        shipment.setTrackingId(request.getTrackingId());
        shipment.setDescription(request.getDescription());
        shipment.setGoodsType(request.getGoodsType());
        shipment.setCarrier(request.getCarrier());
        shipment.setOriginPort(request.getOriginPort());
        shipment.setDestinationPort(request.getDestinationPort());
        shipment.setWeightKg(request.getWeightKg());
        shipment.setEstimatedTimeOfArrival(request.getEstimatedTimeOfArrival());
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public void archiveShipment(UUID id, String userEmail) {
        Shipment shipment = getShipmentById(id, userEmail);
        shipment.setArchived(true);
        shipment.setStatus("ARCHIVED");
        shipmentRepository.save(shipment);
    }

    public Double getTotalLandedCost(String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));
        return shipmentRepository.findByUser(user).stream()
                .filter(s -> !s.isArchived())
                .mapToDouble(s -> calculateLandedCost(s))
                .sum();
    }

    public Double calculateLandedCost(Shipment shipment) {
        if (shipment.getWeightKg() == null) return 0.0;
        double weight = shipment.getWeightKg();
        double shipping = weight * 2.95;
        double harbour = 200.0;
        double duties = weight * 0.354;
        double transport = 150.0;
        return shipping + harbour + duties + transport;
    }
}
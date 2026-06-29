package com.example.ImportEase.service;

import com.example.ImportEase.model.dto.ShipmentRequest;
import com.example.ImportEase.model.AppUser;
import com.example.ImportEase.model.Shipment;
import com.example.ImportEase.repository.AppUserRepository;
import com.example.ImportEase.repository.ShipmentRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final AppUserRepository userRepository;

    public ShipmentService(ShipmentRepository shipmentRepository, AppUserRepository userRepository) {
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }


    @Transactional
    public Shipment createShipment(ShipmentRequest request, String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        if (shipmentRepository.existsByTrackingId(request.getTrackingId())) {
            throw new IllegalArgumentException("A shipment with this tracking ID already exists.");
        }

        Shipment shipment = new Shipment(
                request.getTrackingId(),
                request.getDescription(),
                request.getGoodsType(),
                request.getCarrier(),
                request.getEstimatedTimeOfArrival(),
                user
        );

        return shipmentRepository.save(shipment);
    }

    /**
     * Returns all active (unarchived) shipments for a logged-in user
     */
    public List<Shipment> getActiveShipments(String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));
        return shipmentRepository.findByUser(user).stream()
                .filter(shipment -> !shipment.isArchived())
                .toList();
    }

    /**
     * Returns a specific shipment, verifying ownership for security
     */
    public Shipment getShipmentById(UUID id, String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return shipmentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found or unauthorized access."));
    }

    /**
     * Updates shipment parameters (such as updating ETA or status)
     */
    @Transactional
    public Shipment updateShipment(UUID id, ShipmentRequest request, String userEmail) {
        Shipment shipment = getShipmentById(id, userEmail);

        shipment.setTrackingId(request.getTrackingId());
        shipment.setDescription(request.getDescription());
        shipment.setGoodsType(request.getGoodsType());
        shipment.setCarrier(request.getCarrier());
        shipment.setEstimatedTimeOfArrival(request.getEstimatedTimeOfArrival());

        return shipmentRepository.save(shipment);
    }

    /**
     * Performs a soft delete by archiving the shipment
     */
    @Transactional
    public void archiveShipment(UUID id, String userEmail) {
        Shipment shipment = getShipmentById(id, userEmail);
        shipment.setArchived(true);
        shipment.setStatus("ARCHIVED");
        shipmentRepository.save(shipment);
    }
}
package com.example.importease.controller;

import com.example.importease.dto.ShipmentRequest;
import com.example.importease.model.Shipment;
import com.example.importease.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/shipments")
@CrossOrigin(origins = "*")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<Shipment> createShipment(
            @Valid @RequestBody ShipmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Shipment shipment = shipmentService.createShipment(request, userDetails.getUsername());
        return ResponseEntity.ok(shipment);
    }

    @GetMapping
    public ResponseEntity<List<Shipment>> getMyShipments(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(shipmentService.getActiveShipments(userDetails.getUsername()));
    }

    @GetMapping("/total-cost")
    public ResponseEntity<Map<String, Double>> getTotalLandedCost(
            @AuthenticationPrincipal UserDetails userDetails) {
        Double total = shipmentService.getTotalLandedCost(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("totalLandedCost", total));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Shipment> updateShipment(
            @PathVariable UUID id,
            @Valid @RequestBody ShipmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(shipmentService.updateShipment(id, request, userDetails.getUsername()));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archiveShipment(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        shipmentService.archiveShipment(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
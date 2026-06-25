package com.example.ImportEase.controllers;

import com.example.ImportEase.dtos.ShipmentRequest;
import com.example.ImportEase.models.Shipment;
import com.example.ImportEase.services.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    /**
     * POST /api/shipments - Manually create a new shipment
     */
    @PostMapping
    public ResponseEntity<?> createShipment(@Valid @RequestBody ShipmentRequest request, Principal principal) {
        try {
            Shipment shipment = shipmentService.createShipment(request, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(shipment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/shipments - Retrieve all active shipments for logged-in user
     */
    @GetMapping
    public ResponseEntity<List<Shipment>> getActiveShipments(Principal principal) {
        List<Shipment> shipments = shipmentService.getActiveShipments(principal.getName());
        return ResponseEntity.ok(shipments);
    }

    /**
     * GET /api/shipments/{id} - Get specific shipment details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getShipmentById(@PathVariable UUID id, Principal principal) {
        try {
            Shipment shipment = shipmentService.getShipmentById(id, principal.getName());
            return ResponseEntity.ok(shipment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/shipments/{id} - Update a shipment record
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateShipment(@PathVariable UUID id, @Valid @RequestBody ShipmentRequest request, Principal principal) {
        try {
            Shipment updatedShipment = shipmentService.updateShipment(id, request, principal.getName());
            return ResponseEntity.ok(updatedShipment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/shipments/{id} - Soft-delete/Archive a shipment
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> archiveShipment(@PathVariable UUID id, Principal principal) {
        try {
            shipmentService.archiveShipment(id, principal.getName());
            return ResponseEntity.ok(Map.of("message", "Shipment archived successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }
}

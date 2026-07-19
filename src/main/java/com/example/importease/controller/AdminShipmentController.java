package com.example.importease.controller;

import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentStage;
import com.example.importease.service.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.importease.model.dto.AdminShipmentSummary;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/shipments")
@CrossOrigin(origins = "*")
public class AdminShipmentController {

    private final ShipmentService shipmentService;

    public AdminShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    // GET /api/admin/shipments - list ALL shipments across all users
    @GetMapping
    public ResponseEntity<List<AdminShipmentSummary>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    // POST /api/admin/shipments/{id}/advance-stage
    @PostMapping("/{id}/advance-stage")
    public ResponseEntity<Shipment> advanceStage(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String stageName = body.get("stageName"); // ORIGIN, TRANSIT, AT_PORT, CUSTOMS, DELIVERED
        String note = body.getOrDefault("note", null);
        return ResponseEntity.ok(shipmentService.advanceStage(id, stageName, note));
    }

    // GET /api/admin/shipments/{id}/stages
    @GetMapping("/{id}/stages")
    public ResponseEntity<List<ShipmentStage>> getStages(@PathVariable UUID id) {
        return ResponseEntity.ok(shipmentService.getStageHistory(id));
    }
}

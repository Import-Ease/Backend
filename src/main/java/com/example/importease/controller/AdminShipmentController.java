package com.example.importease.controller;

import com.example.importease.config.LoggingFilter;
import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentCheckpoint;
import com.example.importease.model.ShipmentDocument;
import com.example.importease.model.ShipmentEventLog;
import com.example.importease.model.ShipmentStage;
import com.example.importease.service.ShipmentCheckpointService;
import com.example.importease.service.ShipmentDocumentService;
import com.example.importease.service.ShipmentEventLogService;
import com.example.importease.service.ShipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.example.importease.model.dto.AdminShipmentSummary;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/shipments")
@CrossOrigin(origins = "*")
public class AdminShipmentController {

    private static final Logger log = LoggerFactory.getLogger(AdminShipmentController.class);

    private final ShipmentService shipmentService;
    private final ShipmentDocumentService documentService;
    private final ShipmentCheckpointService checkpointService;
    private final ShipmentEventLogService eventLogService;

    public AdminShipmentController(ShipmentService shipmentService,
                                   ShipmentDocumentService documentService,
                                   ShipmentCheckpointService checkpointService,
                                   ShipmentEventLogService eventLogService) {
        this.shipmentService = shipmentService;
        this.documentService = documentService;
        this.checkpointService = checkpointService;
        this.eventLogService = eventLogService;
    }

    // GET /api/admin/shipments - list ALL shipments across all users
    @GetMapping
    public ResponseEntity<List<AdminShipmentSummary>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    // GET /api/admin/shipments/{id} - get full shipment details
    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentDetail(@PathVariable UUID id) {
        return shipmentService.getShipmentByIdAdmin(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/admin/shipments/{id} - update shipment fields (admin)
    @PutMapping("/{id}")
    public ResponseEntity<Shipment> updateShipment(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Admin updating shipment: id={} | fields={} | by={} | correlationId={}",
                id, body.keySet(), userDetails.getUsername(), LoggingFilter.correlationId());
        return ResponseEntity.ok(shipmentService.adminUpdateShipment(id, body, userDetails.getUsername()));
    }

    // POST /api/admin/shipments/{id}/advance-stage
    @PostMapping("/{id}/advance-stage")
    public ResponseEntity<Shipment> advanceStage(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String stageName = body.get("stageName");
        String note = body.getOrDefault("note", null);
        String email = userDetails.getUsername();
        log.info("Admin advancing shipment stage: id={} | stage={} | by={} | correlationId={}",
                id, stageName, email, LoggingFilter.correlationId());
        return ResponseEntity.ok(shipmentService.advanceStage(id, stageName, note, email));
    }

    // GET /api/admin/shipments/{id}/stages
    @GetMapping("/{id}/stages")
    public ResponseEntity<List<ShipmentStage>> getStages(@PathVariable UUID id) {
        return ResponseEntity.ok(shipmentService.getStageHistory(id));
    }

    // POST /api/admin/shipments/{id}/documents - upload a document
    @PostMapping("/{id}/documents")
    public ResponseEntity<ShipmentDocument> uploadDocument(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        log.info("Admin uploading document: shipmentId={} | type={} | file={} | by={} | correlationId={}",
                id, documentType, file.getOriginalFilename(), email, LoggingFilter.correlationId());
        return ResponseEntity.ok(documentService.uploadDocument(id, documentType, file, email));
    }

    // GET /api/admin/shipments/{id}/documents - list documents
    @GetMapping("/{id}/documents")
    public ResponseEntity<List<ShipmentDocument>> getDocuments(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getDocuments(id));
    }

    // POST /api/admin/shipments/{id}/checkpoints - add a checkpoint
    @PostMapping("/{id}/checkpoints")
    public ResponseEntity<ShipmentCheckpoint> addCheckpoint(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String location = body.get("location");
        String description = body.get("description");
        LocalDateTime timestamp = body.containsKey("timestamp")
                ? LocalDateTime.parse(body.get("timestamp"))
                : LocalDateTime.now();
        log.info("Admin adding checkpoint: shipmentId={} | location={} | by={} | correlationId={}",
                id, location, userDetails.getUsername(), LoggingFilter.correlationId());
        return ResponseEntity.ok(checkpointService.addCheckpoint(id, location, description, timestamp, userDetails.getUsername()));
    }

    // GET /api/admin/shipments/{id}/checkpoints - list checkpoints
    @GetMapping("/{id}/checkpoints")
    public ResponseEntity<List<ShipmentCheckpoint>> getCheckpoints(@PathVariable UUID id) {
        return ResponseEntity.ok(checkpointService.getCheckpoints(id));
    }

    // GET /api/admin/shipments/{id}/checkpoints/stream - SSE stream for real-time tracking
    @GetMapping(value = "/{id}/checkpoints/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCheckpoints(@PathVariable UUID id) {
        return checkpointService.subscribe(id);
    }

    // GET /api/admin/shipments/{id}/events - event log
    @GetMapping("/{id}/events")
    public ResponseEntity<List<ShipmentEventLog>> getEventLog(@PathVariable UUID id) {
        return ResponseEntity.ok(eventLogService.getEventLog(id));
    }
}

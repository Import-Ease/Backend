package com.example.importease.service;

import com.example.importease.config.LoggingFilter;
import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentCheckpoint;
import com.example.importease.model.ShipmentEventLog;
import com.example.importease.repository.ShipmentCheckpointRepository;
import com.example.importease.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ShipmentCheckpointService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentCheckpointService.class);

    private final ShipmentRepository shipmentRepository;
    private final ShipmentCheckpointRepository checkpointRepository;
    private final Map<UUID, List<SseEmitter>> emitters = new HashMap<>();

    private final ShipmentEventLogService eventLogService;

    public ShipmentCheckpointService(ShipmentRepository shipmentRepository,
                                     ShipmentCheckpointRepository checkpointRepository,
                                     ShipmentEventLogService eventLogService) {
        this.shipmentRepository = shipmentRepository;
        this.checkpointRepository = checkpointRepository;
        this.eventLogService = eventLogService;
    }

    private static final int MAX_EMITTERS_PER_SHIPMENT = 10;
    private static final long SSE_TIMEOUT_MS = 3600_000L;

    @Transactional
    public ShipmentCheckpoint addCheckpoint(UUID shipmentId, String location,
                                            String description, LocalDateTime timestamp,
                                            String performedBy) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));

        ShipmentCheckpoint cp = new ShipmentCheckpoint(shipment, location, description, timestamp);
        cp = checkpointRepository.save(cp);

        eventLogService.logEvent(shipmentId, ShipmentEventLog.EventType.CHECKPOINT_ADDED,
                "Checkpoint at " + location + ": " + description, performedBy);

        log.info("Checkpoint added: shipmentId={} | location={} | by={} | correlationId={}",
                shipmentId, location, performedBy, LoggingFilter.correlationId());

        broadcast(shipmentId, cp);
        return cp;
    }

    public List<ShipmentCheckpoint> getCheckpoints(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));
        return checkpointRepository.findByShipmentOrderByTimestampAsc(shipment);
    }

    public SseEmitter subscribe(UUID shipmentId) {
        synchronized (emitters) {
            List<SseEmitter> existing = emitters.get(shipmentId);
            if (existing != null && existing.size() >= MAX_EMITTERS_PER_SHIPMENT) {
                throw new IllegalStateException("Maximum subscribers reached for shipment " + shipmentId);
            }
        }
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        synchronized (emitters) {
            emitters.computeIfAbsent(shipmentId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        }
        emitter.onCompletion(() -> removeEmitter(shipmentId, emitter));
        emitter.onTimeout(() -> removeEmitter(shipmentId, emitter));
        emitter.onError(e -> removeEmitter(shipmentId, emitter));
        return emitter;
    }

    private void broadcast(UUID shipmentId, ShipmentCheckpoint checkpoint) {
        List<SseEmitter> list;
        synchronized (emitters) {
            list = emitters.get(shipmentId);
        }
        if (list == null || list.isEmpty()) return;

        Iterator<SseEmitter> iter = list.iterator();
        while (iter.hasNext()) {
            SseEmitter emitter = iter.next();
            try {
                emitter.send(SseEmitter.event()
                        .name("checkpoint")
                        .data(checkpoint));
            } catch (IOException e) {
                emitter.completeWithError(e);
                iter.remove();
            }
        }
    }

    private void removeEmitter(UUID shipmentId, SseEmitter emitter) {
        synchronized (emitters) {
            List<SseEmitter> list = emitters.get(shipmentId);
            if (list != null) {
                list.remove(emitter);
                if (list.isEmpty()) {
                    emitters.remove(shipmentId);
                }
            }
        }
    }
}

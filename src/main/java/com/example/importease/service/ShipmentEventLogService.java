package com.example.importease.service;

import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentEventLog;
import com.example.importease.model.ShipmentEventLog.EventType;
import com.example.importease.repository.ShipmentEventLogRepository;
import com.example.importease.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ShipmentEventLogService {

    private final ShipmentEventLogRepository logRepository;
    private final ShipmentRepository shipmentRepository;

    public ShipmentEventLogService(ShipmentEventLogRepository logRepository,
                                   ShipmentRepository shipmentRepository) {
        this.logRepository = logRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public ShipmentEventLog logEvent(UUID shipmentId, EventType eventType, String description, String performedBy) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));
        ShipmentEventLog log = new ShipmentEventLog(shipment, eventType, description, performedBy);
        return logRepository.save(log);
    }

    public List<ShipmentEventLog> getEventLog(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));
        return logRepository.findByShipmentOrderByCreatedAtDesc(shipment);
    }
}

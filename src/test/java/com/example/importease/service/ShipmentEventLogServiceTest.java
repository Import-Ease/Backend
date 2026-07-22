package com.example.importease.service;

import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentEventLog;
import com.example.importease.model.ShipmentEventLog.EventType;
import com.example.importease.repository.ShipmentEventLogRepository;
import com.example.importease.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentEventLogServiceTest {

    @Mock ShipmentRepository shipmentRepository;
    @Mock ShipmentEventLogRepository logRepository;

    ShipmentEventLogService service;
    Shipment shipment;
    UUID shipmentId;

    @BeforeEach
    void setUp() {
        service = new ShipmentEventLogService(logRepository, shipmentRepository);
        shipmentId = UUID.randomUUID();
        shipment = new Shipment();
        shipment.setId(shipmentId);
    }

    @Test
    void logEvent_savesAndReturns() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(logRepository.save(any(ShipmentEventLog.class))).thenAnswer(i -> i.getArgument(0));

        ShipmentEventLog log = service.logEvent(shipmentId, EventType.STAGE_ADVANCED,
                "Stage advanced from A to B", "admin@example.com");

        assertEquals(EventType.STAGE_ADVANCED, log.getEventType());
        assertEquals("Stage advanced from A to B", log.getDescription());
        assertEquals("admin@example.com", log.getPerformedBy());
        verify(logRepository).save(any(ShipmentEventLog.class));
    }

    @Test
    void logEvent_shipmentNotFound_throws() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.logEvent(shipmentId, EventType.STAGE_ADVANCED, "desc", "admin"));
    }

    @Test
    void getEventLog_returnsList() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(logRepository.findByShipmentOrderByCreatedAtDesc(shipment))
                .thenReturn(List.of(new ShipmentEventLog()));

        List<ShipmentEventLog> logs = service.getEventLog(shipmentId);
        assertEquals(1, logs.size());
    }
}

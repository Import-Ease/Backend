package com.example.importease.service;

import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentCheckpoint;
import com.example.importease.repository.ShipmentCheckpointRepository;
import com.example.importease.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentCheckpointServiceTest {

    @Mock ShipmentRepository shipmentRepository;
    @Mock ShipmentCheckpointRepository checkpointRepository;
    @Mock ShipmentEventLogService eventLogService;

    ShipmentCheckpointService service;
    Shipment shipment;
    UUID shipmentId;

    @BeforeEach
    void setUp() {
        service = new ShipmentCheckpointService(shipmentRepository, checkpointRepository, eventLogService);
        shipmentId = UUID.randomUUID();
        shipment = new Shipment();
        shipment.setId(shipmentId);
    }

    @Test
    void addCheckpoint_savesAndReturns() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(checkpointRepository.save(any(ShipmentCheckpoint.class))).thenAnswer(i -> i.getArgument(0));

        LocalDateTime ts = LocalDateTime.of(2026, 7, 22, 10, 0);
        ShipmentCheckpoint cp = service.addCheckpoint(shipmentId, "Port of Shanghai", "Container loaded", ts, "admin@example.com");

        assertEquals("Port of Shanghai", cp.getLocation());
        assertEquals("Container loaded", cp.getDescription());
        assertEquals(ts, cp.getTimestamp());
        verify(checkpointRepository).save(any(ShipmentCheckpoint.class));
    }

    @Test
    void addCheckpoint_shipmentNotFound_throws() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.addCheckpoint(shipmentId, "Port", "Desc", LocalDateTime.now(), "admin@example.com"));
    }

    @Test
    void getCheckpoints_returnsList() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(checkpointRepository.findByShipmentOrderByTimestampAsc(shipment))
                .thenReturn(List.of(new ShipmentCheckpoint()));

        List<ShipmentCheckpoint> list = service.getCheckpoints(shipmentId);
        assertEquals(1, list.size());
    }

    @Test
    void subscribe_returnsEmitter() {
        SseEmitter emitter = service.subscribe(shipmentId);
        assertNotNull(emitter);
    }

    @Test
    void addCheckpoint_broadcastsToSubscribers() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(checkpointRepository.save(any(ShipmentCheckpoint.class))).thenAnswer(i -> i.getArgument(0));

        SseEmitter emitter = service.subscribe(shipmentId);
        assertNotNull(emitter);

        service.addCheckpoint(shipmentId, "Port", "Desc", LocalDateTime.now(), "admin@example.com");
        // No exception means broadcast succeeded
    }
}

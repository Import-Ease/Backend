package com.example.importease.service;

import com.example.importease.config.LoggingFilter;
import com.example.importease.dto.OrderRequest;
import com.example.importease.dto.ShipmentRequest;
import com.example.importease.model.AppUser;
import com.example.importease.model.Shipment;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.importease.model.ShipmentStage;
import com.example.importease.model.ShipmentStatus;
import com.example.importease.repository.ShipmentStageRepository;
import com.example.importease.model.ShipmentEventLog;
import com.example.importease.model.dto.AdminShipmentSummary;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    private final ShipmentRepository shipmentRepository;
    private final AppUserRepository userRepository;

    private final ShipmentStageRepository shipmentStageRepository;
    private final ShipmentEventLogService eventLogService;

    public ShipmentService(ShipmentRepository shipmentRepository, AppUserRepository userRepository,
                           ShipmentStageRepository shipmentStageRepository,
                           ShipmentEventLogService eventLogService) {
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.shipmentStageRepository = shipmentStageRepository;
        this.eventLogService = eventLogService;
    }

    // Admin: get ALL shipments across all users
    public List<AdminShipmentSummary> getAllShipments() {
        return shipmentRepository.findAllWithUser().stream()
                .map(AdminShipmentSummary::new)
                .toList();
    }
    // Admin: get shipment by id (no owner guard)
    public Optional<Shipment> getShipmentByIdAdmin(UUID id) {
        return shipmentRepository.findById(id);
    }

    // Admin: update shipment fields map (tracking, description, carrier, etc. + paySupplier)
    @Transactional
    public Shipment adminUpdateShipment(UUID id, Map<String, Object> fields, String adminEmail) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));

        if (fields.containsKey("trackingId")) shipment.setTrackingId((String) fields.get("trackingId"));
        if (fields.containsKey("description")) shipment.setDescription((String) fields.get("description"));
        if (fields.containsKey("goodsType")) shipment.setGoodsType((String) fields.get("goodsType"));
        if (fields.containsKey("carrier")) shipment.setCarrier((String) fields.get("carrier"));
        if (fields.containsKey("originPort")) shipment.setOriginPort((String) fields.get("originPort"));
        if (fields.containsKey("destinationPort")) shipment.setDestinationPort((String) fields.get("destinationPort"));
        if (fields.containsKey("weightKg")) shipment.setWeightKg(fields.get("weightKg") instanceof Number ? ((Number) fields.get("weightKg")).doubleValue() : null);
        if (fields.containsKey("estimatedTimeOfArrival") && fields.get("estimatedTimeOfArrival") instanceof String) {
            shipment.setEstimatedTimeOfArrival(java.time.LocalDate.parse((String) fields.get("estimatedTimeOfArrival")));
        }
        if (fields.containsKey("status")) {
            String newStatus = (String) fields.get("status");
            ShipmentStageValidator.validateTransition(shipment.getStatus().name(), newStatus);
            shipment.setStatus(ShipmentStatus.valueOf(newStatus));
            eventLogService.logEvent(id, ShipmentEventLog.EventType.SHIPMENT_UPDATED,
                    "Status updated to " + newStatus, adminEmail);
        }
        if (fields.containsKey("productId")) shipment.setProductId(fields.get("productId") instanceof Number ? ((Number) fields.get("productId")).longValue() : null);
        if (fields.containsKey("shippingMode")) shipment.setShippingMode((String) fields.get("shippingMode"));
        if (fields.containsKey("orderQuantity")) shipment.setOrderQuantity(fields.get("orderQuantity") instanceof Number ? ((Number) fields.get("orderQuantity")).intValue() : null);
        if (fields.containsKey("paySupplier")) shipment.setPaySupplier((String) fields.get("paySupplier"));

        return shipmentRepository.save(shipment);
    }

    // Admin manually advances a shipment to a new stage
    @Transactional
    public Shipment advanceStage(UUID shipmentId, String stageName, String note, String adminEmail) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));

        ShipmentStatus prevStatus = shipment.getStatus();
        ShipmentStageValidator.validateTransition(prevStatus.name(), stageName);

        ShipmentStage stage = new ShipmentStage(
                shipment, stageName, note,
                adminEmail, ShipmentStage.UpdatedByType.ADMIN
        );
        shipmentStageRepository.save(stage);

        shipment.setStatus(ShipmentStatus.valueOf(stageName));
        shipment = shipmentRepository.save(shipment);

        eventLogService.logEvent(shipmentId, ShipmentEventLog.EventType.STAGE_ADVANCED,
                "Stage advanced from " + prevStatus + " to " + stageName, adminEmail);

        log.info("Shipment stage advanced: {} {} -> {} by {} | correlationId={}",
                shipment.getTrackingId(), prevStatus, stageName, adminEmail, LoggingFilter.correlationId());
        return shipment;
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
            log.warn("Duplicate tracking ID: {} | user={} | correlationId={}",
                    request.getTrackingId(), userEmail, LoggingFilter.correlationId());
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

        shipment = shipmentRepository.save(shipment);
        log.info("Shipment created: trackingId={} | user={} | correlationId={}",
                shipment.getTrackingId(), userEmail, LoggingFilter.correlationId());
        return shipment;
    }

    @Transactional
    public Shipment createOrder(OrderRequest request, String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        String trackingId = generateOrderTrackingId();

        Shipment shipment = new Shipment();
        shipment.setTrackingId(trackingId);
        shipment.setDescription("Order for product #" + request.getProductId());
        shipment.setGoodsType(request.getShippingMode());
        shipment.setCarrier("PENDING");
        shipment.setOriginPort("PENDING");
        shipment.setDestinationPort(request.getDestination());
        shipment.setWeightKg(null);
        shipment.setEstimatedTimeOfArrival(null);
        shipment.setStatus(ShipmentStatus.PENDING_PAYMENT);
        shipment.setArchived(false);
        shipment.setUser(user);
        shipment.setProductId(request.getProductId());
        shipment.setShippingMode(request.getShippingMode());
        shipment.setOrderQuantity(request.getQuantity());

        shipment = shipmentRepository.save(shipment);
        log.info("Order created: trackingId={} | productId={} | user={} | correlationId={}",
                trackingId, request.getProductId(), userEmail, LoggingFilter.correlationId());
        return shipment;
    }

    private String generateOrderTrackingId() {
        String trackingId;
        do {
            long num = ThreadLocalRandom.current().nextLong(100000, 999999);
            trackingId = "ORD-" + num;
        } while (shipmentRepository.existsByTrackingId(trackingId));
        return trackingId;
    }

    public List<Shipment> getActiveShipments(String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));
        return shipmentRepository.findByUserAndArchivedFalse(user);
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
        shipment.setStatus(ShipmentStatus.ARCHIVED);
        shipmentRepository.save(shipment);
    }

    public Double getTotalLandedCost(String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));
        return shipmentRepository.findByUserAndArchivedFalse(user).stream()
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
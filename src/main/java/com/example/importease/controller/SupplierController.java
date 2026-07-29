package com.example.importease.controller;

import com.example.importease.dto.ShipmentResponse;
import com.example.importease.model.AppUser;
import com.example.importease.model.Product;
import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentPaymentStatus;
import com.example.importease.model.Supplier;
import com.example.importease.model.dto.SupplierOrderSummary;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.repository.ProductRepository;
import com.example.importease.repository.ShipmentRepository;
import com.example.importease.repository.SupplierRepository;
import com.example.importease.service.PaystackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaystackService paystackService;

    @Autowired
    private ShipmentRepository shipmentRepository;

    // GET all suppliers
    @GetMapping
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    // GET supplier by id
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        Optional<Supplier> supplier = supplierRepository.findById(id);

        return supplier.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET public supplier profile with stats
    @GetMapping("/{id}/public")
    public ResponseEntity<?> getPublicProfile(@PathVariable Long id) {
        Optional<Supplier> supplierOpt = supplierRepository.findById(id);
        if (supplierOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Supplier supplier = supplierOpt.get();
        long productCount = productRepository.countBySupplierId(id);
        List<Long> productIds = productRepository.findBySupplierId(id).stream()
                .map(Product::getId).toList();
        List<Shipment> shipments = productIds.isEmpty()
                ? List.of()
                : shipmentRepository.findByProductIdInWithUser(productIds);
        long ordersCompleted = shipments.stream()
                .filter(s -> "DELIVERED".equals(s.getStatus().name()))
                .count();
        long totalOrders = shipments.size();

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", supplier.getId());
        profile.put("name", supplier.getName());
        profile.put("email", supplier.getEmail());
        profile.put("phone", supplier.getPhone());
        profile.put("address", supplier.getAddress());
        profile.put("description", supplier.getDescription());
        profile.put("logoUrl", supplier.getLogoUrl());
        profile.put("category", supplier.getCategory());
        profile.put("shippingOrigin", supplier.getShippingOrigin());
        profile.put("subscriptionTier", supplier.getSubscriptionTier());
        profile.put("createdAt", supplier.getCreatedAt());
        profile.put("productCount", productCount);
        profile.put("totalOrders", totalOrders);
        profile.put("ordersCompleted", ordersCompleted);
        profile.put("verified", "PAID".equals(supplier.getSubscriptionTier()));

        return ResponseEntity.ok(profile);
    }

    // GET products by supplier id
    @GetMapping("/{id}/products")
    public ResponseEntity<List<Product>> getSupplierProducts(@PathVariable Long id) {
        if (!supplierRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productRepository.findBySupplierId(id));
    }

    // GET the logged-in supplier's own profile
    @GetMapping("/me")
    public ResponseEntity<?> getMySupplier(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = appUserRepository.findByEmail(userDetails.getUsername())
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found."));
        }

        Optional<Supplier> supplierOpt = supplierRepository.findByOwnerId(currentUser.getId());

        if (supplierOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No supplier profile found. Please create one first."));
        }

        return ResponseEntity.ok(supplierOpt.get());
    }

    // POST create the logged-in supplier's own profile
    @PostMapping("/me")
    public ResponseEntity<?> createMySupplier(
            @RequestBody Supplier supplier,
            @AuthenticationPrincipal UserDetails userDetails) {

        AppUser currentUser = appUserRepository.findByEmail(userDetails.getUsername())
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found."));
        }

        if (supplierRepository.findByOwnerId(currentUser.getId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "You already have a supplier profile."));
        }

        supplier.setOwnerId(currentUser.getId());
        Supplier saved = supplierRepository.save(supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT update the logged-in supplier's own profile
    @PutMapping("/me")
    public ResponseEntity<?> updateMySupplier(
            @RequestBody Supplier updatedSupplier,
            @AuthenticationPrincipal UserDetails userDetails) {

        AppUser currentUser = appUserRepository.findByEmail(userDetails.getUsername())
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found."));
        }

        Optional<Supplier> supplierOpt = supplierRepository.findByOwnerId(currentUser.getId());

        if (supplierOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No supplier profile found."));
        }

        Supplier supplier = supplierOpt.get();
        supplier.setName(updatedSupplier.getName());
        supplier.setEmail(updatedSupplier.getEmail());
        supplier.setPhone(updatedSupplier.getPhone());
        supplier.setAddress(updatedSupplier.getAddress());
        supplier.setDescription(updatedSupplier.getDescription());
        supplier.setLogoUrl(updatedSupplier.getLogoUrl());
        supplier.setCategory(updatedSupplier.getCategory());
        supplier.setShippingOrigin(updatedSupplier.getShippingOrigin());

        return ResponseEntity.ok(supplierRepository.save(supplier));
    }

    // GET product count + tier for the logged-in supplier
    @GetMapping("/me/product-count")
    public ResponseEntity<?> getMyProductCount(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = appUserRepository.findByEmail(userDetails.getUsername())
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found."));
        }

        Optional<Supplier> supplierOpt = supplierRepository.findByOwnerId(currentUser.getId());

        if (supplierOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No supplier profile found."));
        }

        Supplier supplier = supplierOpt.get();
        long count = productRepository.countBySupplierId(supplier.getId());

        return ResponseEntity.ok(Map.of(
                "productCount", count,
                "subscriptionTier", supplier.getSubscriptionTier(),
                "paidUntil", supplier.getPaidUntil() != null ? supplier.getPaidUntil().toString() : null
        ));
    }

    // GET all orders (shipments) placed against the logged-in supplier's products
    @GetMapping("/me/orders")
    public ResponseEntity<?> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = appUserRepository.findByEmail(userDetails.getUsername())
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found."));
        }

        Optional<Supplier> supplierOpt = supplierRepository.findByOwnerId(currentUser.getId());

        if (supplierOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No supplier profile found."));
        }

        List<Long> productIds = productRepository.findBySupplierId(supplierOpt.get().getId())
                .stream()
                .map(Product::getId)
                .toList();

        if (productIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Shipment> shipments = shipmentRepository.findByProductIdInWithUser(productIds);
        List<ShipmentResponse> response = shipments.stream()
                .map(ShipmentResponse::fromEntityWithUser)
                .toList();

        return ResponseEntity.ok(response);
    }

    // GET dashboard stats for the logged-in supplier
    @GetMapping("/me/dashboard")
    public ResponseEntity<?> getMyDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = appUserRepository.findByEmail(userDetails.getUsername())
                .orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found."));
        }
        Optional<Supplier> supplierOpt = supplierRepository.findByOwnerId(currentUser.getId());
        if (supplierOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No supplier profile found."));
        }
        Supplier supplier = supplierOpt.get();
        long productCount = productRepository.countBySupplierId(supplier.getId());
        List<Long> productIds = productRepository.findBySupplierId(supplier.getId()).stream()
                .map(Product::getId).toList();
        List<Shipment> shipments = productIds.isEmpty()
                ? List.of()
                : shipmentRepository.findByProductIdInWithUser(productIds);
        long activeShipments = shipments.stream()
                .filter(s -> s.getStatus() != null
                        && !"DELIVERED".equals(s.getStatus().name())
                        && !"ARCHIVED".equals(s.getStatus().name()))
                .count();
        long pendingOrders = shipments.stream()
                .filter(s -> s.getStatus() != null
                        && ("PENDING_PAYMENT".equals(s.getStatus().name())
                            || "ORDER_CREATED".equals(s.getStatus().name())))
                .count();
        long completedDeliveries = shipments.stream()
                .filter(s -> "DELIVERED".equals(s.getStatus().name()))
                .count();
        double revenue = shipments.stream()
                .filter(s -> s.getPaymentStatus() == ShipmentPaymentStatus.PAID
                        && s.getAmountPaid() != null)
                .mapToDouble(s -> s.getAmountPaid().doubleValue())
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("productCount", productCount);
        stats.put("activeShipments", activeShipments);
        stats.put("pendingOrders", pendingOrders);
        stats.put("completedDeliveries", completedDeliveries);
        stats.put("revenue", revenue);
        stats.put("recentOrders", shipments.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(ShipmentResponse::fromEntityWithUser)
                .toList());
        return ResponseEntity.ok(stats);
    }

    // GET order stats (counts by status bucket) for the logged-in supplier's products
    @GetMapping("/me/orders/summary")
    public ResponseEntity<?> getMyOrdersSummary(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = appUserRepository.findByEmail(userDetails.getUsername())
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found."));
        }

        Optional<Supplier> supplierOpt = supplierRepository.findByOwnerId(currentUser.getId());

        if (supplierOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No supplier profile found."));
        }

        List<Long> productIds = productRepository.findBySupplierId(supplierOpt.get().getId())
                .stream()
                .map(Product::getId)
                .toList();

        List<Shipment> shipments = productIds.isEmpty()
                ? List.of()
                : shipmentRepository.findByProductIdInWithUser(productIds);

        return ResponseEntity.ok(SupplierOrderSummary.from(shipments));
    }

    // POST upgrade to PAID tier — initializes a $4 USD Paystack payment
    @PostMapping("/me/upgrade")
    public ResponseEntity<?> upgradeToPaid(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = appUserRepository.findByEmail(userDetails.getUsername())
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found."));
        }

        Optional<Supplier> supplierOpt = supplierRepository.findByOwnerId(currentUser.getId());

        if (supplierOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No supplier profile found. Please create one first."));
        }

        Supplier supplier = supplierOpt.get();

        if ("PAID".equals(supplier.getSubscriptionTier())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You are already on the PAID tier."));
        }

        try {
            Map<String, Object> result = paystackService.initializePayment(
                    currentUser.getEmail(),
                    supplier.getName(),
                    new BigDecimal("4.00"),
                    "USD",
                    "SUBSCRIPTION",
                    supplier.getId()
            );
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // POST add supplier (legacy/admin direct add - kept for backward compatibility)
    @PostMapping
    public Supplier addSupplier(@RequestBody Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    // PUT update supplier by id (legacy/admin direct update)
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(
            @PathVariable Long id,
            @RequestBody Supplier updatedSupplier) {

        return supplierRepository.findById(id)
                .map(supplier -> {
                    supplier.setName(updatedSupplier.getName());
                    supplier.setEmail(updatedSupplier.getEmail());
                    supplier.setPhone(updatedSupplier.getPhone());
                    supplier.setAddress(updatedSupplier.getAddress());
                    supplier.setDescription(updatedSupplier.getDescription());
                    supplier.setLogoUrl(updatedSupplier.getLogoUrl());
                    supplier.setCategory(updatedSupplier.getCategory());
                    supplier.setShippingOrigin(updatedSupplier.getShippingOrigin());

                    return ResponseEntity.ok(supplierRepository.save(supplier));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE supplier
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {

        if (!supplierRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        supplierRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
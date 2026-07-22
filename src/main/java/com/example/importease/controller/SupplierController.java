package com.example.importease.controller;

import com.example.importease.model.AppUser;
import com.example.importease.model.Supplier;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.repository.ProductRepository;
import com.example.importease.repository.SupplierRepository;
import com.example.importease.service.PaystackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
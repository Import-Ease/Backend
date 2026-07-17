package com.example.importease.controller;

import com.example.importease.model.AppUser;
import com.example.importease.model.Product;
import com.example.importease.model.Supplier;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.repository.ProductRepository;
import com.example.importease.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    // Helper: get the current logged-in user's supplier profile
    private Supplier getCurrentUserSupplier(UserDetails userDetails) {
        AppUser currentUser = appUserRepository.findByEmail(userDetails.getUsername())
                .orElse(null);
        if (currentUser == null) return null;
        return supplierRepository.findByOwnerId(currentUser.getId()).orElse(null);
    }

    // View All Products (public, unchanged)
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // View Product By ID (public, unchanged)
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);

        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET only the logged-in supplier's own products
    @GetMapping("/mine")
    public ResponseEntity<?> getMyProducts(@AuthenticationPrincipal UserDetails userDetails) {
        Supplier supplier = getCurrentUserSupplier(userDetails);

        if (supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No supplier profile found. Please create one first."));
        }

        List<Product> myProducts = productRepository.findAll().stream()
                .filter(p -> p.getSupplier() != null && p.getSupplier().getId().equals(supplier.getId()))
                .toList();

        return ResponseEntity.ok(myProducts);
    }

    // Add Product - now requires login, auto-attaches to logged-in supplier
    @PostMapping
    public ResponseEntity<?> addProduct(
            @RequestBody Product product,
            @AuthenticationPrincipal UserDetails userDetails) {

        Supplier supplier = getCurrentUserSupplier(userDetails);

        if (supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No supplier profile found. Please create one first via POST /api/suppliers/me."));
        }

        product.setSupplier(supplier);
        Product saved = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Update Product - only the owning supplier (or admin) can update
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();
        Supplier supplier = getCurrentUserSupplier(userDetails);
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        boolean isOwner = supplier != null && product.getSupplier() != null
                && product.getSupplier().getId().equals(supplier.getId());

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only edit your own products."));
        }

        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setQuantity(updatedProduct.getQuantity());
        product.setImageUrl(updatedProduct.getImageUrl());

        return ResponseEntity.ok(productRepository.save(product));
    }

    // Delete Product - only the owning supplier (or admin) can delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();
        Supplier supplier = getCurrentUserSupplier(userDetails);
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        boolean isOwner = supplier != null && product.getSupplier() != null
                && product.getSupplier().getId().equals(supplier.getId());

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only delete your own products."));
        }

        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
package com.example.importease.repository;

import com.example.importease.model.Product;
import com.example.importease.model.SearchResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySupplierId(Long supplierId);

    long countBySupplierId(Long supplierId);

    @Query(value = "SELECT p.id, p.name, p.price, " +
            "s.name, s.phone, s.shipping_origin, p.image_url " +
            "FROM products p " +
            "JOIN suppliers s ON p.supplier_id = s.id " +
            "WHERE similarity(LOWER(p.name), LOWER(:query)) > 0.3 " +
            "OR similarity(LOWER(p.description), LOWER(:query)) > 0.3",
            nativeQuery = true)
    List<Object[]> searchProductsRaw(@Param("query") String query);

    default List<SearchResponseDto> searchProducts(String query) {
        return searchProductsRaw(query).stream()
                .map(row -> new SearchResponseDto(
                        ((Number) row[0]).longValue(),   // productId
                        (String) row[1],                 // productName
                        ((Number) row[2]).doubleValue(), // productPrice
                        (String) row[3],                 // supplierName
                        (String) row[4],                 // supplierContact (phone)
                        (String) row[5],                 // originCountry
                        (String) row[6]                  // imageUrl
                )).toList();
    }

    @Query("SELECT p.name FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT(:prefix, '%'))")
    List<String> findTop5NamesByPrefix(@Param("prefix") String prefix);
}
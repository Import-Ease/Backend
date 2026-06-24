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

    // Tuesday's Core Engine: Upgraded to match products with their international suppliers
    @Query("SELECT new com.example.importease.model.SearchResponseDto(p.id, p.name, p.price, s.name, s.country, s.contact) " +
            "FROM Product p JOIN p.supplier s " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<SearchResponseDto> searchProducts(@Param("query") String query);

    // Wednesday's Engine: Fetches just the top 5 product names matching the prefix for dropdown suggestions
    @Query("SELECT p.name FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT(:prefix, '%'))")
    List<String> findTop5NamesByPrefix(@Param("prefix") String prefix);
}
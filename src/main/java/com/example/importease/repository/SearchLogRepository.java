package com.example.importease.repository;

import com.example.importease.model.SearchLog;
import com.example.importease.model.SearchResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    @Query("SELECT new com.example.importease.model.SearchResponseDto(" +
            "p.id, p.name, CAST(p.price AS double), " +
            "p.supplier.name, p.supplier.phone, p.supplier.shippingOrigin, p.imageUrl) " +
            "FROM Product p " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<SearchResponseDto> searchProducts(@Param("query") String query);

    @Query("SELECT p.name FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT(:prefix, '%'))")
    List<String> findProjectedNamesByPrefix(@Param("prefix") String prefix);

    List<SearchLog> findTop10ByOrderByTimestampDesc();
}
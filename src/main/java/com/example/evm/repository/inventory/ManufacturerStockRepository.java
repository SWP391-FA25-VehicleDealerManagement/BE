package com.example.evm.repository.inventory;

import com.example.evm.entity.inventory.ManufacturerStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManufacturerStockRepository extends JpaRepository<ManufacturerStock, Long> {

    Optional<ManufacturerStock> findByVariantVariantIdAndColor(Long variantId, String color);

    @Query("SELECT ms FROM ManufacturerStock ms " +
           "LEFT JOIN FETCH ms.variant v " +
           "LEFT JOIN FETCH v.model m")
    List<ManufacturerStock> findAllWithRelations();
    
}
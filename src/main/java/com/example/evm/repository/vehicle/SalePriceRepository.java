package com.example.evm.repository.vehicle;

import com.example.evm.entity.vehicle.SalePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalePriceRepository extends JpaRepository<SalePrice, Integer> {

    SalePrice findTopByDealerIdAndVariantIdOrderByEffectiveDateDesc(Long dealerId, Long variantId);

    @Query("SELECT s.price FROM SalePrice s " +
           "WHERE s.variantId = :variantId AND s.dealerId = :dealerId " +
           "ORDER BY s.effectiveDate DESC " +
           "LIMIT 1")
    Optional<BigDecimal> findActivePriceByVariantIdAndDealerId(
            @Param("variantId") Long variantId, 
            @Param("dealerId") Long dealerId
    );
}
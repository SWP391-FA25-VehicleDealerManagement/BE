package com.example.evm.repository.vehicle;

import com.example.evm.entity.vehicle.SalePrice;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.stereotype.Repository;
=======
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
import java.util.Optional;

@Repository
public interface SalePriceRepository extends JpaRepository<SalePrice, Long> {

<<<<<<< HEAD
    SalePrice findTopByDealerIdAndVariantIdOrderByEffectiveDateDesc(Long dealerId, Long variantId);

    Optional<SalePrice> findTopByVariantIdAndDealerIdOrderByEffectiveDateDesc(Long variantId, Long dealerId);
=======
    SalePrice findTopByDealerIdAndVariantIdOrderByEffectiveDateDesc(Integer dealerId, Integer variantId);

    @Query("SELECT s.price FROM SalePrice s " +
           "WHERE s.variantId = :variantId AND s.dealerId = :dealerId " +
           "ORDER BY s.effectiveDate DESC " +
           "LIMIT 1")
    Optional<BigDecimal> findActivePriceByVariantIdAndDealerId(
            @Param("variantId") Integer variantId, 
            @Param("dealerId") Integer dealerId
    );
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
}
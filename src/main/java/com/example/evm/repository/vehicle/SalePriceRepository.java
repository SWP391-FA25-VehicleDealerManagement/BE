package com.example.evm.repository.vehicle;

import com.example.evm.entity.vehicle.SalePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SalePriceRepository extends JpaRepository<SalePrice, Long> {

    SalePrice findTopByDealerIdAndVariantIdOrderByEffectiveDateDesc(Long dealerId, Long variantId);

    Optional<SalePrice> findTopByVariantIdAndDealerIdOrderByEffectiveDateDesc(Long variantId, Long dealerId);
}
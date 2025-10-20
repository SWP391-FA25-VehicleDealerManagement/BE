package com.example.evm.repository.vehicle;

import com.example.evm.entity.vehicle.SalePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalePriceRepository extends JpaRepository<SalePrice, Long> {

    /**
     * Lấy mức giá mới nhất đang có hiệu lực cho một Dealer và Variant cụ thể.
     */
    Optional<SalePrice> findTopByDealerDealerIdAndVariantVariantIdOrderByEffectiveDateDesc(
        Long dealerId, 
        Long variantId
    );
}
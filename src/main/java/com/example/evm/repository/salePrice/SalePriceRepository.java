package com.example.evm.repository.salePrice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.evm.entity.salePrice.SalePrice;

/**
 * Repository for SalePrice entity
 * ✅ Chỉ chứa các query dựa trên schema thực tế: dealer_id, variant_id, price, effectivedate
 */
@Repository
public interface SalePriceRepository extends JpaRepository<SalePrice, Long> {
    
    // Tìm theo dealer
    List<SalePrice> findByDealerId(Long dealerId);
    
    // Tìm theo variant
    List<SalePrice> findByVariantId(Long variantId);
    
    // Tìm theo dealer và variant
    @Query("SELECT sp FROM SalePrice sp WHERE sp.dealerId = :dealerId AND sp.variantId = :variantId")
    List<SalePrice> findByDealerIdAndVariantId(@Param("dealerId") Long dealerId, @Param("variantId") Long variantId);
    
    // Tìm giá đang hiệu lực (effectiveDate <= today)
    @Query("SELECT sp FROM SalePrice sp WHERE sp.variantId = :variantId AND sp.effectiveDate <= :today ORDER BY sp.effectiveDate DESC")
    List<SalePrice> findActivePricesByVariant(@Param("variantId") Long variantId, @Param("today") LocalDate today);
    
    // Tìm giá theo khoảng giá
    @Query("SELECT sp FROM SalePrice sp WHERE sp.price BETWEEN :minPrice AND :maxPrice")
    List<SalePrice> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    // Tìm giá mới nhất của dealer cho variant
    @Query("SELECT sp FROM SalePrice sp WHERE sp.dealerId = :dealerId AND sp.variantId = :variantId ORDER BY sp.effectiveDate DESC")
    Optional<SalePrice> findLatestPriceByDealerAndVariant(@Param("dealerId") Long dealerId, @Param("variantId") Long variantId);
}

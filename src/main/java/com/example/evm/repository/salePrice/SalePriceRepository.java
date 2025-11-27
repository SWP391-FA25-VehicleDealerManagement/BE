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
 *  Chỉ chứa các query dựa trên schema thực tế: dealer_id, variant_id, price, effectivedate
 */
@Repository
public interface SalePriceRepository extends JpaRepository<SalePrice, Long> {
    
    // Lấy tất cả giá bán - với variant và model
    @Query("SELECT sp FROM SalePrice sp LEFT JOIN FETCH sp.variant v LEFT JOIN FETCH v.model")
    List<SalePrice> findAllWithVariantAndModel();
    
    // Tìm theo ID - với variant và model
    @Query("SELECT sp FROM SalePrice sp LEFT JOIN FETCH sp.variant v LEFT JOIN FETCH v.model WHERE sp.salepriceId = :id")
    Optional<SalePrice> findByIdWithVariantAndModel(@Param("id") Long id);
    
    // Tìm theo dealer - với variant và model
    @Query("SELECT sp FROM SalePrice sp LEFT JOIN FETCH sp.variant v LEFT JOIN FETCH v.model WHERE sp.dealerId = :dealerId")
    List<SalePrice> findByDealerId(@Param("dealerId") Long dealerId);
    
    // Tìm theo variant - với variant và model
    @Query("SELECT sp FROM SalePrice sp LEFT JOIN FETCH sp.variant v LEFT JOIN FETCH v.model WHERE sp.variantId = :variantId")
    List<SalePrice> findByVariantId(@Param("variantId") Long variantId);
    
    // Tìm theo dealer và variant - với variant và model
    @Query("SELECT sp FROM SalePrice sp LEFT JOIN FETCH sp.variant v LEFT JOIN FETCH v.model WHERE sp.dealerId = :dealerId AND sp.variantId = :variantId")
    List<SalePrice> findByDealerIdAndVariantId(@Param("dealerId") Long dealerId, @Param("variantId") Long variantId);
    
    // Tìm giá đang hiệu lực (effectiveDate <= today) - với variant và model
    @Query("SELECT sp FROM SalePrice sp LEFT JOIN FETCH sp.variant v LEFT JOIN FETCH v.model WHERE sp.variantId = :variantId AND sp.effectiveDate <= :today ORDER BY sp.effectiveDate DESC")
    List<SalePrice> findActivePricesByVariant(@Param("variantId") Long variantId, @Param("today") LocalDate today);
    
    // Tìm giá theo khoảng giá - với variant và model
    @Query("SELECT sp FROM SalePrice sp LEFT JOIN FETCH sp.variant v LEFT JOIN FETCH v.model WHERE sp.price BETWEEN :minPrice AND :maxPrice")
    List<SalePrice> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    // Tìm giá mới nhất của dealer cho variant - với variant và model
    @Query("SELECT sp FROM SalePrice sp LEFT JOIN FETCH sp.variant v LEFT JOIN FETCH v.model WHERE sp.dealerId = :dealerId AND sp.variantId = :variantId ORDER BY sp.effectiveDate DESC")
    Optional<SalePrice> findLatestPriceByDealerAndVariant(@Param("dealerId") Long dealerId, @Param("variantId") Long variantId);
}

package com.example.evm.repository.promotion;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.evm.entity.promotion.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    // Tìm khuyến mãi theo dealer
    List<Promotion> findByDealerDealerId(Long dealerId);
    
    // Tìm khuyến mãi toàn hệ thống (dealer = null)
    List<Promotion> findByDealerIsNull();
    
    // Tìm khuyến mãi đang active
    @Query("SELECT p FROM Promotion p WHERE p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findActivePromotions(@Param("today") LocalDate today);
    
    // Tìm khuyến mãi áp dụng cho vehicle cụ thể
    @Query("SELECT p FROM Promotion p JOIN p.vehicles v WHERE v.vehicleId = :vehicleId AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findActivePromotionsByVehicle(@Param("vehicleId") Long vehicleId, @Param("today") LocalDate today);
    
    // Tìm khuyến mãi áp dụng cho dealer cụ thể (bao gồm cả toàn hệ thống)
    @Query("SELECT p FROM Promotion p WHERE (p.dealer.dealerId = :dealerId OR p.dealer IS NULL) AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findActivePromotionsByDealer(@Param("dealerId") Long dealerId, @Param("today") LocalDate today);
    
    // Tìm khuyến mãi sắp hết hạn (trong vòng 7 ngày)
    @Query("SELECT p FROM Promotion p WHERE p.endDate BETWEEN :today AND :nextWeek")
    List<Promotion> findExpiringSoonPromotions(@Param("today") LocalDate today, @Param("nextWeek") LocalDate nextWeek);
}
package com.example.evm.service.promotion;

import com.example.evm.entity.promotion.Promotion;
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.promotion.PromotionRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final VehicleRepository vehicleRepository;

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public List<Promotion> getPromotionsByDealer(Long dealerId) {
        return promotionRepository.findByDealerDealerId(dealerId);
    }

    public List<Promotion> getSystemWidePromotions() {
        return promotionRepository.findByDealerIsNull();
    }

    public List<Promotion> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDate.now());
    }

    public List<Promotion> getActivePromotionsByVehicle(Long vehicleId) {
        return promotionRepository.findActivePromotionsByVehicle(vehicleId, LocalDate.now());
    }

    public List<Promotion> getActivePromotionsByDealer(Long dealerId) {
        return promotionRepository.findActivePromotionsByDealer(dealerId, LocalDate.now());
    }

    public List<Promotion> getExpiringSoonPromotions() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        // Lưu ý: Phương thức này có thể cần sửa đổi trong Repository nếu nó dựa vào created_date
        return promotionRepository.findExpiringSoonPromotions(today, nextWeek); 
    }

    public Promotion getPromotionById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));
    }

    public Promotion createPromotion(Promotion promotion) {
        // Validate dealer exists if specified
        if (promotion.getDealer() != null && promotion.getDealer().getDealerId() != null) {
            // Dealer validation would be here if we had dealer repository injected
        }

        Promotion savedPromotion = promotionRepository.save(promotion);
        log.info("Promotion created: {} with discount {}%", 
                savedPromotion.getTitle(), savedPromotion.getDiscountRate());
        
        return savedPromotion;
    }

    public Promotion updatePromotion(Long id, Promotion promotionDetails) {
        Promotion promotion = getPromotionById(id);
        
        promotion.setTitle(promotionDetails.getTitle());
        promotion.setDescription(promotionDetails.getDescription());
        promotion.setDiscountRate(promotionDetails.getDiscountRate());
        promotion.setStartDate(promotionDetails.getStartDate());
        promotion.setEndDate(promotionDetails.getEndDate());

        // Update dealer if provided
        if (promotionDetails.getDealer() != null) {
            promotion.setDealer(promotionDetails.getDealer());
        }

        Promotion updatedPromotion = promotionRepository.save(promotion);
        log.info("Promotion updated: {}", updatedPromotion.getTitle());
        
        return updatedPromotion;
    }

    public Promotion addVehicleToPromotion(Long promotionId, Long vehicleId) {
        Promotion promotion = getPromotionById(promotionId);
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        
        if (!promotion.getVehicles().contains(vehicle)) {
            promotion.getVehicles().add(vehicle);
        }
        
        return promotionRepository.save(promotion);
    }

    public Promotion removeVehicleFromPromotion(Long promotionId, Long vehicleId) {
        Promotion promotion = getPromotionById(promotionId);
        promotion.getVehicles().removeIf(vehicle -> vehicle.getVehicleId().equals(vehicleId));
        return promotionRepository.save(promotion);
    }

    public void deletePromotion(Long id) {
        Promotion promotion = getPromotionById(id);
        promotionRepository.delete(promotion);
        log.info("Promotion deleted: {}", promotion.getTitle());
    }

    // Tính giá sau khi áp dụng khuyến mãi tốt nhất
    public Double getBestDiscountedPrice(Long vehicleId, Long dealerId, Double originalPrice) {
        List<Promotion> applicablePromotions = getActivePromotionsByVehicle(vehicleId);
        
        Double bestPrice = originalPrice;
        Promotion bestPromotion = null;
        
        for (Promotion promo : applicablePromotions) {
            if (promo.appliesToDealer(dealerId)) {
                Double discountedPrice = promo.calculateDiscountedPrice(originalPrice);
                if (discountedPrice < bestPrice) {
                    bestPrice = discountedPrice;
                    bestPromotion = promo;
                }
            }
        }
        
        if (bestPromotion != null) {
            log.debug("Applied promotion {} to vehicle {}, original: {}, discounted: {}", 
                    bestPromotion.getTitle(), vehicleId, originalPrice, bestPrice);
        }
        
        return bestPrice;
    }
}
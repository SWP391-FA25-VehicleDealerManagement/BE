package com.example.evm.controller.promotion;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.entity.promotion.Promotion;
import com.example.evm.service.promotion.PromotionService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Hidden
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Promotion>>> getAllPromotions() {
        List<Promotion> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(new ApiResponse<>(true, "Promotions retrieved successfully", promotions));
    }   

    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Promotion>>> getPromotionsByDealer(@PathVariable Long dealerId) {
        List<Promotion> promotions = promotionService.getPromotionsByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Promotions retrieved successfully", promotions));
    }

    @GetMapping("/system-wide")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Promotion>>> getSystemWidePromotions() {
        List<Promotion> promotions = promotionService.getSystemWidePromotions();
        return ResponseEntity.ok(new ApiResponse<>(true, "System-wide promotions retrieved", promotions));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Promotion>>> getActivePromotions() {
        List<Promotion> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(new ApiResponse<>(true, "Active promotions retrieved", promotions));
    }

    @GetMapping("/vehicle/{vehicleId}/active")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Promotion>>> getActivePromotionsByVehicle(@PathVariable Long vehicleId) {
        List<Promotion> promotions = promotionService.getActivePromotionsByVehicle(vehicleId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Active promotions for vehicle retrieved", promotions));
    }

    @GetMapping("/dealer/{dealerId}/active")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Promotion>>> getActivePromotionsByDealer(@PathVariable Long dealerId) {
        List<Promotion> promotions = promotionService.getActivePromotionsByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Active promotions for dealer retrieved", promotions));
    }

    @GetMapping("/expiring-soon")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Promotion>>> getExpiringSoonPromotions() {
        List<Promotion> promotions = promotionService.getExpiringSoonPromotions();
        return ResponseEntity.ok(new ApiResponse<>(true, "Expiring soon promotions retrieved", promotions));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Promotion>> getPromotionById(@PathVariable Long id) {
        Promotion promotion = promotionService.getPromotionById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Promotion retrieved successfully", promotion));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<Promotion>> createPromotion(@RequestBody Promotion promotion) {
        Promotion createdPromotion = promotionService.createPromotion(promotion);
        return ResponseEntity.ok(new ApiResponse<>(true, "Promotion created successfully", createdPromotion));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<Promotion>> updatePromotion(
            @PathVariable Long id, 
            @RequestBody Promotion promotion) {
        Promotion updatedPromotion = promotionService.updatePromotion(id, promotion);
        return ResponseEntity.ok(new ApiResponse<>(true, "Promotion updated successfully", updatedPromotion));
    }

    @PostMapping("/{promotionId}/vehicles/{vehicleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<Promotion>> addVehicleToPromotion(
            @PathVariable Long promotionId,
            @PathVariable Long vehicleId) {
        Promotion updatedPromotion = promotionService.addVehicleToPromotion(promotionId, vehicleId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle added to promotion", updatedPromotion));
    }

    @DeleteMapping("/{promotionId}/vehicles/{vehicleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<Promotion>> removeVehicleFromPromotion(
            @PathVariable Long promotionId,
            @PathVariable Long vehicleId) {
        Promotion updatedPromotion = promotionService.removeVehicleFromPromotion(promotionId, vehicleId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle removed from promotion", updatedPromotion));
    }

    /*
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<Promotion>> deactivatePromotion(@PathVariable Long id) {
        promotionService.deactivatePromotion(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Promotion deactivated successfully", null));
    }
    */

    @GetMapping("/calculate-price")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Double>> calculateDiscountedPrice(
            @RequestParam Long vehicleId,
            @RequestParam Long dealerId,
            @RequestParam Double originalPrice) {
        Double discountedPrice = promotionService.getBestDiscountedPrice(vehicleId, dealerId, originalPrice);
        return ResponseEntity.ok(new ApiResponse<>(true, "Discounted price calculated", discountedPrice));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Promotion deleted successfully", null));
    }
}

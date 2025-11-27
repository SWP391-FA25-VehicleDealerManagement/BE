package com.example.evm.controller.salePrice;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.salePrice.CreateSalePriceRequest;
import com.example.evm.dto.salePrice.SalePriceResponse;
import com.example.evm.dto.salePrice.UpdateSalePriceRequest;
import com.example.evm.entity.salePrice.SalePrice;
import com.example.evm.service.salePrice.SalePriceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller để quản lý giá bán của dealer cho từng variant
 * Chỉ chứa các API dựa trên schema thực tế
 */
@Slf4j
@RestController
@RequestMapping("/api/sale-prices")
@RequiredArgsConstructor
public class SalePriceController {
    
    private final SalePriceService salePriceService;

    /**
     * Tạo mới giá bán
     * POST /api/sale-prices
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF','DEALER_MANAGER','DEALER_STAFF')")
    public ResponseEntity<ApiResponse<SalePriceResponse>> createPrice(@Valid @RequestBody CreateSalePriceRequest request) {
        try {
            // Convert DTO to Entity
            SalePrice salePrice = new SalePrice();
            salePrice.setDealerId(request.getDealerId());
            salePrice.setVariantId(request.getVariantId());
            salePrice.setBasePrice(request.getBasePrice());
            salePrice.setPrice(request.getPrice());
            salePrice.setEffectiveDate(request.getEffectiveDate());
            
            SalePrice createdPrice = salePriceService.createPrice(salePrice);
            log.info("Created sale price ID: {} for dealer: {}, variant: {}", 
                createdPrice.getSalepriceId(), request.getDealerId(), request.getVariantId());
            
            // Convert to response DTO
            SalePriceResponse response = new SalePriceResponse(createdPrice);
            
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "Sale price created successfully", 
                    response
            ));
        } catch (Exception e) {
            log.error("Error creating sale price", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, 
                    "Failed to create sale price: " + e.getMessage(), 
                    null
            ));
        }
    }

    /**
     * Lấy tất cả giá bán
     * GET /api/sale-prices
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<List<SalePriceResponse>>> getAllPrices() {
        try {
            List<SalePriceResponse> prices = salePriceService.getAllPrices();
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "All sale prices retrieved successfully", 
                    prices
            ));
        } catch (Exception e) {
            log.error("Error retrieving all sale prices", e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, 
                    "Failed to retrieve sale prices", 
                    null
            ));
        }
    }

    /**
     * Lấy giá bán theo ID
     * GET /api/sale-prices/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<SalePriceResponse>> getPriceById(@PathVariable Long id) {
        try {
            SalePriceResponse price = salePriceService.getPriceById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "Sale price retrieved successfully", 
                    price
            ));
        } catch (Exception e) {
            log.error("Error retrieving sale price with id: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lấy giá bán theo dealer
     * GET /api/sale-prices/dealer/{dealerId}
     */
    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<List<SalePriceResponse>>> getPricesByDealer(@PathVariable Long dealerId) {
        try {
            List<SalePriceResponse> prices = salePriceService.getPricesByDealer(dealerId);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "Sale prices for dealer retrieved successfully", 
                    prices
            ));
        } catch (Exception e) {
            log.error("Error retrieving sale prices for dealer: {}", dealerId, e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, 
                    "Failed to retrieve sale prices for dealer", 
                    null
            ));
        }
    }

    /**
     * Lấy giá bán theo variant
     * GET /api/sale-prices/variant/{variantId}
     */
    @GetMapping("/variant/{variantId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<List<SalePriceResponse>>> getPricesByVariant(@PathVariable Long variantId) {
        try {
            List<SalePriceResponse> prices = salePriceService.getPricesByVariant(variantId);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "Sale prices for variant retrieved successfully", 
                    prices
            ));
        } catch (Exception e) {
            log.error("Error retrieving sale prices for variant: {}", variantId, e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, 
                    "Failed to retrieve sale prices for variant", 
                    null
            ));
        }
    }

    /**
     * Lấy giá bán theo dealer và variant
     * GET /api/sale-prices/dealer/{dealerId}/variant/{variantId}
     */
    @GetMapping("/dealer/{dealerId}/variant/{variantId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<List<SalePriceResponse>>> getPricesByDealerAndVariant(
            @PathVariable Long dealerId,
            @PathVariable Long variantId) {
        try {
            List<SalePriceResponse> prices = salePriceService.getPricesByDealerAndVariant(dealerId, variantId);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "Sale prices retrieved successfully", 
                    prices
            ));
        } catch (Exception e) {
            log.error("Error retrieving sale prices for dealer: {} and variant: {}", dealerId, variantId, e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, 
                    "Failed to retrieve sale prices", 
                    null
            ));
        }
    }

    /**
     * Lấy giá đang hiệu lực cho variant (effectiveDate <= today)
     * GET /api/sale-prices/variant/{variantId}/active
     */
    @GetMapping("/variant/{variantId}/active")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<List<SalePriceResponse>>> getActivePricesByVariant(@PathVariable Long variantId) {
        try {
            List<SalePriceResponse> prices = salePriceService.getActivePricesByVariant(variantId);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "Active sale prices for variant retrieved successfully", 
                    prices
            ));
        } catch (Exception e) {
            log.error("Error retrieving active sale prices for variant: {}", variantId, e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, 
                    "Failed to retrieve active sale prices for variant", 
                    null
            ));
        }
    }

    /**
     * Lấy giá mới nhất của dealer cho variant
     * GET /api/sale-prices/dealer/{dealerId}/variant/{variantId}/latest
     */
    @GetMapping("/dealer/{dealerId}/variant/{variantId}/latest")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<SalePriceResponse>> getLatestPrice(
            @PathVariable Long dealerId,
            @PathVariable Long variantId) {
        try {
            SalePriceResponse price = salePriceService.getLatestPriceByDealerAndVariant(dealerId, variantId);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "Latest sale price retrieved successfully", 
                    price
            ));
        } catch (Exception e) {
            log.error("Error retrieving latest sale price for dealer: {} and variant: {}", dealerId, variantId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lấy giá trong khoảng
     * GET /api/sale-prices/range?minPrice=xxx&maxPrice=yyy
     */
    @GetMapping("/range")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF','DEALER_MANAGER','DEALER_STAFF')")
    public ResponseEntity<ApiResponse<List<SalePriceResponse>>> getPricesByRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        try {
            List<SalePriceResponse> prices = salePriceService.getPricesByRange(minPrice, maxPrice);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "Sale prices in range retrieved successfully", 
                    prices
            ));
        } catch (Exception e) {
            log.error("Error retrieving sale prices in range", e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, 
                    "Failed to retrieve sale prices in range", 
                    null
            ));
        }
    }

    /**
     * Cập nhật giá bán
     * PUT /api/sale-prices/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF','DEALER_MANAGER','DEALER_STAFF')")
    public ResponseEntity<ApiResponse<SalePriceResponse>> updatePrice(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSalePriceRequest request) {
        try {
            // Convert DTO to Entity
            SalePrice priceDetails = new SalePrice();
            priceDetails.setBasePrice(request.getBasePrice());
            priceDetails.setPrice(request.getPrice());
            priceDetails.setEffectiveDate(request.getEffectiveDate());
            
            SalePrice updatedPrice = salePriceService.updatePrice(id, priceDetails);
            log.info("Updated sale price ID: {}", id);
            
            // Convert to response DTO
            SalePriceResponse response = new SalePriceResponse(updatedPrice);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Sale price updated successfully", 
                response
            ));
        } catch (Exception e) {
            log.error("Error updating sale price {}", id, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                false, 
                "Failed to update sale price: " + e.getMessage(), 
                null
            ));
        }
    }

    /**
     * Xóa giá bán
     * DELETE /api/sale-prices/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF','DEALER_MANAGER','DEALER_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deletePrice(@PathVariable Long id) {
        try {
            salePriceService.deletePrice(id);
            log.info("Deleted sale price ID: {}", id);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Sale price deleted successfully", 
                null
            ));
        } catch (Exception e) {
            log.error("Error deleting sale price {}", id, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                false, 
                "Failed to delete sale price: " + e.getMessage(), 
                null
            ));
        }
    }
}

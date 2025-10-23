package com.example.evm.controller.inventory;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.inventory.AllocationResponse;
import com.example.evm.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller InventoryController - APIs allocate/recall xe
 * 
 * Endpoints:
 * - POST /api/inventory/allocate - Phân bổ xe từ kho tổng cho dealer
 * - POST /api/inventory/recall - Thu hồi xe từ dealer về kho tổng
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Phân bổ xe từ kho tổng cho dealer
     * 
     * Request body:
     * {
     *   "dealerId": 1,
     *   "variantId": 6,
     *   "color": "Red",
     *   "quantity": 5
     * }
     */
    @PostMapping("/allocate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<AllocationResponse>> allocateVehicles(
            @RequestBody Map<String, Object> request) {
        
        Long dealerId = Long.valueOf(request.get("dealerId").toString());
        Long variantId = Long.valueOf(request.get("variantId").toString());
        String color = request.get("color").toString();
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        
        log.info("Allocating {} vehicles (variant: {}, color: {}) to dealer {}", 
                quantity, variantId, color, dealerId);
        
        AllocationResponse response = inventoryService.allocateVehiclesToDealer(
                dealerId, variantId, color, quantity);
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                "Vehicles allocated successfully", response));
    }

    /**
     * Thu hồi xe từ dealer về kho tổng
     * 
     * Request body:
     * {
     *   "dealerId": 1,
     *   "variantId": 6,
     *   "color": "Red",
     *   "quantity": 3
     * }
     */
    @PostMapping("/recall")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<String>> recallVehicles(
            @RequestBody Map<String, Object> request) {
        
        Long dealerId = Long.valueOf(request.get("dealerId").toString());
        Long variantId = Long.valueOf(request.get("variantId").toString());
        String color = request.get("color").toString();
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        
        log.info("Recalling {} vehicles (variant: {}, color: {}) from dealer {}", 
                quantity, variantId, color, dealerId);
        
        inventoryService.recallVehiclesFromDealer(dealerId, variantId, color, quantity);
        
        String message = String.format("✅ Successfully recalled %d vehicles from dealer", quantity);
        
        return ResponseEntity.ok(new ApiResponse<>(true, message, message));
    }
}


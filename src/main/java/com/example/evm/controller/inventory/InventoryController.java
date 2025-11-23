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
     *   "requestId": 28,  // ✅ Thêm requestId để xác định request cụ thể
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
        
        Long requestId = request.get("requestId") != null ? 
                Long.valueOf(request.get("requestId").toString()) : null;
        Long dealerId = Long.valueOf(request.get("dealerId").toString());
        Long variantId = Long.valueOf(request.get("variantId").toString());
        String color = request.get("color").toString();
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        
        log.info("Allocating {} vehicles (variant: {}, color: {}) to dealer {} for request {}", 
                quantity, variantId, color, dealerId, requestId);
        
        try {
            AllocationResponse response = inventoryService.allocateVehiclesToDealer(
                    requestId, dealerId, variantId, color, quantity);
            return ResponseEntity.ok(new ApiResponse<>(true,
                    response.getMessage(), response));
        } catch (IllegalStateException ex) {
            // Trả về thông báo không đủ số lượng
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    ex.getMessage(),
                    null
            ));
        }
    }

    /**
     * Thu hồi xe từ dealer về kho tổng
     * 
     * Request body:
     * {
     *   "requestId": 28,  // ✅ Thêm requestId để xác định request cụ thể cần thu hồi
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
        
        Long requestId = request.get("requestId") != null ? 
                Long.valueOf(request.get("requestId").toString()) : null;
        Long dealerId = Long.valueOf(request.get("dealerId").toString());
        Long variantId = Long.valueOf(request.get("variantId").toString());
        String color = request.get("color").toString();
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        
        log.info("Recalling {} vehicles (variant: {}, color: {}) from dealer {} for request {}", 
                quantity, variantId, color, dealerId, requestId);
        
        try {
            inventoryService.recallVehiclesFromDealer(requestId, dealerId, variantId, color, quantity);
            
            // Message sẽ được log trong service, ở đây chỉ trả về success
            String message = String.format("✅ Đã xử lý yêu cầu thu hồi %d xe từ đại lý. " +
                    "Xem log để biết số lượng thực tế được thu hồi.", quantity);
            
            return ResponseEntity.ok(new ApiResponse<>(true, message, message));
        } catch (IllegalStateException ex) {
            // Nếu có exception (không nên xảy ra nữa sau khi sửa, nhưng giữ lại để an toàn)
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    ex.getMessage(),
                    null
            ));
        }
    }
}


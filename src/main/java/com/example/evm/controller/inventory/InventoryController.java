package com.example.evm.controller.inventory;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.inventory.AllocationRequest;
import com.example.evm.dto.inventory.AllocationResponse;
import com.example.evm.dto.inventory.RecallRequest;
import com.example.evm.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
         * Hỗ trợ allocate nhiều variant/màu/số lượng khác nhau cùng lúc
         * 
         * @param requestId Request ID cụ thể cần allocate
         * @param request Request body chứa dealerId và danh sách items (variant, color, quantity)
         * @return AllocationResponse chứa tổng hợp kết quả allocate
         */
        @PostMapping("/allocate/{requestId}")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
        public ResponseEntity<ApiResponse<AllocationResponse>> allocateVehicles(
                        @PathVariable Long requestId,
                        @RequestBody AllocationRequest request) {

                log.info("Allocating {} items to dealer {} for request ID: {}", 
                        request.getItems().size(), request.getDealerId(), requestId);

                try {
                        AllocationResponse response = inventoryService.allocateVehiclesToDealer(
                                        requestId, request.getDealerId(), request.getItems());
                        return ResponseEntity.ok(new ApiResponse<>(true, response.getMessage(), response));
                } catch (IllegalStateException ex) {
                        return ResponseEntity.badRequest().body(new ApiResponse<>(false, ex.getMessage(), null));
                }
        }

        /**
         * Thu hồi xe từ dealer về kho tổng
         * 
         * @param requestId Request ID cụ thể cần thu hồi
         * @param request Request body chứa dealerId
         * @return Success message
         */
        @PostMapping("/recall/{requestId}")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
        public ResponseEntity<ApiResponse<String>> recallVehicles(
                        @PathVariable Long requestId,
                        @RequestBody RecallRequest request) {

                log.info("Recalling vehicles from dealer {} for request ID: {}", 
                                request.getDealerId(), requestId);

                try {
                        inventoryService.recallVehiclesFromDealer(requestId, request.getDealerId());

                        return ResponseEntity.ok(new ApiResponse<>(true, 
                                "Đã thu hồi xe về kho tổng thành công", null));
                } catch (Exception ex) {
                        return ResponseEntity.badRequest().body(new ApiResponse<>(false, ex.getMessage(), null));
                }
        }
}

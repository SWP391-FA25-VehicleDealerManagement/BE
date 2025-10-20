package com.example.evm.controller.inventory;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.inventory.AllocationRequest;
import com.example.evm.dto.inventory.InventoryResponse;
import com.example.evm.dto.inventory.StockRequest;
import com.example.evm.dto.inventory.ManufacturerStockRequest;
import com.example.evm.service.inventory.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /*
    ------------------------------------------------------------------
    -- 1. API KHO ĐẠI LÝ (DEALER STOCK)
    ------------------------------------------------------------------
    */

    // 🔹 GET tất cả kho của ĐẠI LÝ
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    @GetMapping("/dealer")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllDealerStock() {
        List<InventoryResponse> inventoryList = inventoryService.getAllDealerStock();
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer inventory list retrieved", inventoryList));
    }

    // 🔹 THÊM HÀNG vào kho ĐẠI LÝ (hoặc cộng dồn nếu đã có)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
    @PostMapping("/dealer")
    public ResponseEntity<ApiResponse<InventoryResponse>> addOrUpdateDealerStock(
            @Valid @RequestBody StockRequest request) { // Sửa: Dùng DTO
        
        InventoryResponse response = inventoryService.addOrUpdateDealerStock(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer stock updated successfully", response));
    }

    // 🔹 CẬP NHẬT TRẠNG THÁI kho ĐẠI LÝ
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
    @PatchMapping("/dealer/{id}/status")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateDealerStockStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String newStatus = body.get("status");
        InventoryResponse response = inventoryService.updateStockStatus(id, newStatus);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer stock status updated", response));
    }

    // 🔹 XÓA một mặt hàng khỏi kho ĐẠI LÝ
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @DeleteMapping("/dealer/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDealerStock(@PathVariable Long id) { // Sửa: Dùng Long
        inventoryService.deleteStock(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer stock item deleted", null));
    }

    /*
    ------------------------------------------------------------------
    -- 2. API KHO TỔNG (MANUFACTURER STOCK)
    ------------------------------------------------------------------
    */

    // 🔹 GET tất cả KHO TỔNG
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @GetMapping("/manufacturer")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllManufacturerStock() {
        List<InventoryResponse> inventoryList = inventoryService.getAllManufacturerStock();
        return ResponseEntity.ok(new ApiResponse<>(true, "Manufacturer stock list retrieved", inventoryList));
    }

    // 🔹 NHẬP XE (từ nhà máy) vào KHO TỔNG
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @PostMapping("/manufacturer")
    public ResponseEntity<ApiResponse<InventoryResponse>> addOrUpdateManufacturerStock(
            @Valid @RequestBody ManufacturerStockRequest request) { 

        StockRequest stockRequestForService = mapToStockRequest(request); 

        InventoryResponse response = inventoryService.addOrUpdateManufacturerStock(stockRequestForService);
        return ResponseEntity.ok(new ApiResponse<>(true, "Manufacturer stock updated", response));
    }
    
    // 🔹 CẬP NHẬT TRẠNG THÁI KHO TỔNG (VD: "In Production" -> "Ready")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @PatchMapping("/manufacturer/{id}/status")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateManufacturerStockStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String newStatus = body.get("status");
        InventoryResponse response = inventoryService.updateManufacturerStockStatus(id, newStatus);
        return ResponseEntity.ok(new ApiResponse<>(true, "Manufacturer stock status updated", response));
    }

    /*
    ------------------------------------------------------------------
    -- 3. API ĐIỀU PHỐI (Giữa 2 kho)
    ------------------------------------------------------------------
    */

    // 🔹 ĐIỀU PHỐI (Kho tổng -> Đại lý)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @PostMapping("/allocate")
    public ResponseEntity<ApiResponse<InventoryResponse>> allocateStock(
            @Valid @RequestBody AllocationRequest request) {
        
        InventoryResponse response = inventoryService.allocateStockToDealer(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock allocated successfully to dealer", response));
    }

    // 🔹 THU HỒI (Đại lý -> Kho tổng)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @PostMapping("/recall")
    public ResponseEntity<ApiResponse<String>> recallStock(
            @Valid @RequestBody AllocationRequest request) {
        
        String message = inventoryService.recallStockFromDealer(request);
        return ResponseEntity.ok(new ApiResponse<>(true, message, message));
    }

    private StockRequest mapToStockRequest(ManufacturerStockRequest manuRequest) {
        StockRequest stockReq = new StockRequest();
        stockReq.setVariantId(manuRequest.getVariantId());
        stockReq.setColor(manuRequest.getColor());
        stockReq.setQuantity(manuRequest.getQuantity());
        stockReq.setStatus(manuRequest.getStatus());
        return stockReq;
    }
}
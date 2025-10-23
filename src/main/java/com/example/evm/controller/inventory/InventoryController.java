package com.example.evm.controller.inventory;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.inventory.AllocationRequest;
import com.example.evm.dto.inventory.AllocationResponse;
import com.example.evm.dto.inventory.InventoryResponse;
import com.example.evm.dto.inventory.StockRequest;
import com.example.evm.dto.inventory.ManufacturerStockResponse;
import com.example.evm.service.inventory.InventoryService;
import com.example.evm.dto.inventory.UpdateStockRequest;
import com.example.evm.dto.inventory.UpdateManufacturerStockRequest;
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
    
    // 🔹 GET kho của dealer cụ thể (theo dealerId)
    // ✅ API này lấy từ InventoryStock thay vì Vehicle
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    @GetMapping("/dealer/{dealerId}")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getDealerStockByDealerId(
            @PathVariable Long dealerId) {
        List<InventoryResponse> inventoryList = inventoryService.getDealerStockByDealerId(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer inventory retrieved successfully", inventoryList));
    }

    // 🔹 THÊM HÀNG vào kho ĐẠI LÝ (hoặc cộng dồn nếu đã có)
    @PostMapping("/dealer")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryResponse>> createDealerStock( // <-- Đổi tên hàm
            @Valid @RequestBody StockRequest request) {
        InventoryResponse response = inventoryService.createDealerStock(request); // <-- Gọi hàm create
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer stock created successfully", response));
    }

    @PutMapping("/dealer/{id}") // <-- Dùng PUT và có ID
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateDealerStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) { // <-- Dùng DTO Update
        InventoryResponse response = inventoryService.updateDealerStock(id, request); // <-- Gọi hàm update
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
    public ResponseEntity<ApiResponse<Void>> deleteDealerStock(@PathVariable Long id) {
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
    public ResponseEntity<ApiResponse<List<ManufacturerStockResponse>>> getAllManufacturerStock() {
        List<ManufacturerStockResponse> inventoryList = inventoryService.getAllManufacturerStock();
        return ResponseEntity.ok(new ApiResponse<>(true, "Manufacturer stock list retrieved", inventoryList));
    }

    // 🔹 NHẬP XE (từ nhà máy) vào KHO TỔNG
    @PostMapping("/manufacturer")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<ManufacturerStockResponse>> createManufacturerStock(
            @Valid @RequestBody StockRequest request) { // Vẫn dùng StockRequest cho Create
        ManufacturerStockResponse response = inventoryService.createManufacturerStock(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Manufacturer stock created", response));
    }

    @PutMapping("/manufacturer/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<ManufacturerStockResponse>> updateManufacturerStock(
             @PathVariable Long id,
             @Valid @RequestBody UpdateManufacturerStockRequest request) { 
        ManufacturerStockResponse response = inventoryService.updateManufacturerStock(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Manufacturer stock updated", response));
    }
    
    // 🔹 CẬP NHẬT TRẠNG THÁI KHO TỔNG (VD: "In Production" -> "Ready")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @PatchMapping("/manufacturer/{id}/status")
    public ResponseEntity<ApiResponse<ManufacturerStockResponse>> updateManufacturerStockStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String newStatus = body.get("status");
        ManufacturerStockResponse response = inventoryService.updateManufacturerStockStatus(id, newStatus);
        return ResponseEntity.ok(new ApiResponse<>(true, "Manufacturer stock status updated", response));
    }

    /*
    ------------------------------------------------------------------
    -- 3. API ĐIỀU PHỐI (Giữa 2 kho)
    ------------------------------------------------------------------
    */

    // 🔹 ĐIỀU PHỐI (Kho tổng -> Đại lý)
    // ✅ Tự động update status DealerRequest: APPROVED → SHIPPED
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @PostMapping("/allocate")
    public ResponseEntity<ApiResponse<AllocationResponse>> allocateStock(
            @Valid @RequestBody AllocationRequest request) {
        AllocationResponse response = inventoryService.allocateStockToDealer(request);
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
}
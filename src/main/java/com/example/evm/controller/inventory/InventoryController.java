package com.example.evm.controller.inventory;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.inventory.InventoryResponse;
import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.service.inventory.InventoryService;
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

    // 🔹 GET all
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAll() {
        List<InventoryResponse> inventoryList = inventoryService.getAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory list retrieved successfully", inventoryList));
    }

    // 🔹 ADD Stock
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addStock(@RequestBody InventoryStock stock) {
        inventoryService.addStock(stock);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory added successfully", null));
    }

    // 🔹 UPDATE Stock
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateStock(@PathVariable Integer id, @RequestBody InventoryStock stock) {
        inventoryService.updateStock(id, stock);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory updated successfully", null));
    }

    // 🔹 DELETE Stock
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStock(@PathVariable Integer id) {
        inventoryService.deleteStock(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory deleted successfully", null));
    }

    // 🔹 ALLOCATE Vehicle
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @PostMapping("/allocate")
    public ResponseEntity<ApiResponse<String>> allocateVehicle(@RequestBody Map<String, Integer> request) {
        // Chú ý: request.get("vehicleId") trả về Integer. Nếu service cần String, cần chuyển đổi.
        // Giả sử service chấp nhận Integer/Long/String và bạn đang gửi đúng kiểu dữ liệu.
        String message = inventoryService.allocateVehicleToDealer(
                request.get("vehicleId"), request.get("dealerId"), request.get("quantity")
        );
        // Trả về message dưới dạng data
        return ResponseEntity.ok(new ApiResponse<>(true, message, message));
    }

    // 🔹 RECALL Vehicle
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @PostMapping("/recall")
    public ResponseEntity<ApiResponse<String>> recallVehicle(@RequestBody Map<String, Integer> request) {
        String message = inventoryService.recallVehicleFromDealer(
                request.get("vehicleId"), request.get("dealerId"), request.get("quantity")
        );
        // Trả về message dưới dạng data
        return ResponseEntity.ok(new ApiResponse<>(true, message, message));
    }
}
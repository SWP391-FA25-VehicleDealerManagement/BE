package com.example.evm.controller.vehicle;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.vehicle.StockSummaryResponse;
import com.example.evm.dto.vehicle.VehicleFullResponse;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.service.vehicle.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller VehicleController - APIs quản lý xe
 * 
 * Endpoints:
 * - POST /api/vehicles - Tạo xe mới
 * - GET /api/vehicles/{id} - Lấy full info 1 xe
 * - GET /api/vehicles/manufacturer/stock - Tổng hợp kho tổng
 * - GET /api/vehicles/manufacturer/vehicles - Chi tiết xe trong kho tổng
 * - GET /api/vehicles/dealer/{dealerId}/stock - Tổng hợp kho dealer
 * - GET /api/vehicles/dealer/{dealerId}/vehicles - Chi tiết xe dealer
 */
@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * Tạo xe mới (tự động vào kho tổng)
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleFullResponse>> createVehicle(
            @Valid @RequestBody VehicleRequest request) {
        
        log.info("Creating vehicle - variantId: {}, color: {}", request.getVariantId(), request.getColor());
        
        VehicleFullResponse response = vehicleService.createVehicle(request);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle created successfully", response));
    }

    /**
     * Lấy thông tin chi tiết 1 xe (kèm full info)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleFullResponse>> getVehicleById(@PathVariable Long id) {
        
        log.info("Fetching vehicle by id: {}", id);
        
        VehicleFullResponse response = vehicleService.getVehicleById(id);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle retrieved successfully", response));
    }

    // ===== APIs KHO TỔNG =====

    /**
     * Lấy tổng hợp kho tổng (GROUP BY variant + color)
     */
    @GetMapping("/manufacturer/stock")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<StockSummaryResponse>>> getManufacturerStockSummary() {
        
        log.info("Fetching manufacturer stock summary");
        
        List<StockSummaryResponse> response = vehicleService.getManufacturerStockSummary();
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
            "Manufacturer stock summary retrieved successfully", response));
    }

    /**
     * Lấy chi tiết tất cả xe trong kho tổng (với VIN)
     */
    @GetMapping("/manufacturer/vehicles")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<VehicleFullResponse>>> getManufacturerVehicles() {
        
        log.info("Fetching all manufacturer vehicles");
        
        List<VehicleFullResponse> response = vehicleService.getAllManufacturerVehicles();
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
            "Manufacturer vehicles retrieved successfully", response));
    }

    // ===== APIs KHO DEALER =====

    /**
     * Lấy tổng hợp kho dealer (GROUP BY variant + color)
     */
    @GetMapping("/dealer/{dealerId}/stock")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<StockSummaryResponse>>> getDealerStockSummary(
            @PathVariable Long dealerId) {
        
        log.info("Fetching dealer stock summary for dealer: {}", dealerId);
        
        List<StockSummaryResponse> response = vehicleService.getDealerStockSummary(dealerId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
            "Dealer stock summary retrieved successfully", response));
    }

    /**
     * Lấy chi tiết tất cả xe của dealer (với VIN)
     */
    @GetMapping("/dealer/{dealerId}/vehicles")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<VehicleFullResponse>>> getDealerVehicles(
            @PathVariable Long dealerId) {
        
        log.info("Fetching all dealer vehicles for dealer: {}", dealerId);
        
        List<VehicleFullResponse> response = vehicleService.getDealerVehicles(dealerId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
            "Dealer vehicles retrieved successfully", response));
    }
}


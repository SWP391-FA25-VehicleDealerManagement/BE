package com.example.evm.controller.vehicle;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.vehicle.StockSummaryResponse;
import com.example.evm.dto.vehicle.VehicleFullResponse;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.service.storage.FileStorageService;
import com.example.evm.service.vehicle.VehicleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import com.example.evm.dto.vehicle.DealerVehicleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
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
@Validated
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final FileStorageService fileStorageService;
    
    // Lấy tất cả xe
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<VehicleFullResponse>>> getAllVehicles() {
        List<VehicleFullResponse> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(new ApiResponse<>(true, "All vehicles retrieved successfully", vehicles));
    }

    /**
     * Tạo xe mới (tự động vào kho tổng)
     */
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleFullResponse>> createVehicle(
            @RequestParam("vinNumber") @NotBlank String vinNumber,
            @RequestParam("variantId") @NotNull Long variantId,
            @RequestParam("color") @NotBlank String color,
            @Parameter(in = ParameterIn.DEFAULT, description = "Tick nếu là xe lái thử")
            @RequestParam(value = "isTestDrive", required = false) Boolean isTestDrive,
            @RequestPart(value = "file", required = true) MultipartFile file) {

        log.info("Creating vehicle - VIN number: {}, variantId: {}, color: {}", vinNumber, variantId, color);

        VehicleRequest requestDto = new VehicleRequest();
        requestDto.setVinNumber(vinNumber);
        requestDto.setVariantId(variantId);
        requestDto.setColor(color);
        requestDto.setTestDrive(isTestDrive);

        VehicleFullResponse response = vehicleService.createVehicle(requestDto, file);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle created successfully", response));
    }
    
    // CẬP NHẬT SỐ VIN, ẢNH VÀ MÀU XE
    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleFullResponse>> updateVehicleVisuals(
            @Parameter(description = "ID của xe cần cập nhật")
            @PathVariable Long id,

            @Parameter(in = ParameterIn.DEFAULT, description = "Số VIN mới (tùy chọn)")
            @RequestParam(value = "vinNumber", required = false) String vinNumber,
            
            @Parameter(in = ParameterIn.DEFAULT, description = "Màu sắc mới (tùy chọn)")
            @RequestParam(value = "color", required = false) String color,
            
            @Parameter(description = "Ảnh thực tế mới (tùy chọn)")
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        VehicleFullResponse updated = vehicleService.updateVehicle(id, vinNumber, color, file);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle updated successfully", updated));
    }

    // ĐẶT STATUS XE THÀNH "TEST_DRIVE"
    @PutMapping("/{id}/test-drive")
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER')")
    @Operation(summary = "Đặt trạng thái xe thành TEST_DRIVE")
    public ResponseEntity<ApiResponse<String>> markVehicleAsTestDrive(
            @PathVariable("id") Long vehicleId) {

        vehicleService.setVehicleAsTestDrive(vehicleId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Xe đã được đánh dấu là xe lái thử (TEST_DRIVE)", null));
    }

    // ĐẶT STATUS XE VỀ LẠI KHO DEALER
    @PutMapping("/{id}/return-test-drive")
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER')")
    @Operation(summary = "Đặt trạng thái xe thành IN_DEALER_STOCK")
    public ResponseEntity<ApiResponse<String>> returnVehicleFromTestDrive(
            @PathVariable("id") Long vehicleId) {

        vehicleService.returnVehicleFromTestDrive(vehicleId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Xe đã được chuyển về kho đại lý", null));
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

    // XÓA XE
    @DeleteMapping("/{vehicleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long vehicleId) {
        vehicleService.deleteVehicle(vehicleId);
        return ResponseEntity.ok("Vehicle deleted successfully.");
    }

    // LẤY ẢNH XE THEO FILENAME
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource file = fileStorageService.load("vehicles", filename);
        String contentType = "application/octet-stream"; // Mặc định
        try {
            // Cố gắng tự động xác định ContentType từ file
            contentType = Files.probeContentType(file.getFile().toPath());
        } catch (IOException e) {
            log.error("Could not determine file type for variant image: {}", filename, e);
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
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
    public ResponseEntity<ApiResponse<List<DealerVehicleResponse>>> getDealerVehicles(
            @PathVariable Long dealerId) {
        List<DealerVehicleResponse> vehicles = vehicleService.getDealerVehicles(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched dealer vehicles successfully", vehicles));
    }

    /**
     * Lấy tất cả xe có status = TEST_DRIVE (cho test drive)
     * Có thể filter theo dealerId (optional)
     */
    @GetMapping("/test-drive")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<VehicleFullResponse>>> getTestDriveVehicles(
            @RequestParam(required = false) Long dealerId) {
        
        List<VehicleFullResponse> vehicles;
        
        if (dealerId != null) {
            log.info("Fetching test drive vehicles for dealer: {}", dealerId);
            vehicles = vehicleService.getTestDriveVehiclesByDealer(dealerId);
            return ResponseEntity.ok(new ApiResponse<>(true, 
                "Test drive vehicles for dealer retrieved successfully", vehicles));
        } else {
            log.info("Fetching all test drive vehicles");
            vehicles = vehicleService.getTestDriveVehicles();
            return ResponseEntity.ok(new ApiResponse<>(true, 
                "Test drive vehicles retrieved successfully", vehicles));
        }
    }

}


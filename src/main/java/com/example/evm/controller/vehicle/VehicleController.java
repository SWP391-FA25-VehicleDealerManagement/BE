package com.example.evm.controller.vehicle;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;
import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.example.evm.service.storage.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;      

import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {

    private final VehicleService vehicleService;
    private final FileStorageService fileStorageService;

    // 🟢 GET all active vehicles
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER', 'ADMIN', 'EVM_STAFF')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllActiveVehicles() {
        List<VehicleResponse> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(new ApiResponse<>(true, "Active vehicles retrieved successfully", vehicles));
    }

    // 🔴 GET all inactive vehicles
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER', 'ADMIN', 'EVM_STAFF')")
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllInactiveVehicles() {
        List<VehicleResponse> vehicles = vehicleService.getAllInactiveVehicles();
        return ResponseEntity.ok(new ApiResponse<>(true, "Inactive vehicles retrieved successfully", vehicles));
    }

    // 🏢 GET vehicles by dealer ID
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER', 'ADMIN', 'EVM_STAFF')")
    @GetMapping("/dealer/{dealerId}")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getVehiclesByDealerId(@PathVariable Long dealerId) {
        List<VehicleResponse> vehicles = vehicleService.getVehiclesByDealerId(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicles for dealer retrieved successfully", vehicles));
    }

    // 🆔 GET vehicle by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER', 'ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable Long id) {
        VehicleResponse vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle retrieved successfully", vehicle));
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource file = fileStorageService.load(filename);
        String contentType = "application/octet-stream";
        try {
             // Cố gắng tự động xác định ContentType từ file
             contentType = Files.probeContentType(file.getFile().toPath());
        } catch (IOException e) {
             // Nếu có lỗi, ghi log lại
             log.error("Could not determine file type for filename: {}", filename, e);
        }

        // Nếu không xác định được, vẫn dùng loại mặc định
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        // Trả về file với Content-Type đã được xác định
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    // 🔍 SEARCH by name
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER', 'ADMIN', 'EVM_STAFF')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> searchVehiclesByName(@RequestParam String name) {
        List<VehicleResponse> vehicles = vehicleService.searchVehiclesByName(name);
        return ResponseEntity.ok(new ApiResponse<>(true, "Search completed successfully", vehicles));
    }

    // ➕ ADD new vehicle
    @PostMapping 
    @PreAuthorize("hasAnyAuthority('DEALER_MANAGER', 'ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleResponse>> addVehicle(
            @RequestBody VehicleRequest request) {
        VehicleResponse created = vehicleService.addVehicle(request, null); 
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle added successfully", created));
    }

    // 🔄 UPDATE existing vehicle
    @PutMapping(value = "/{id}")
    @PreAuthorize("hasAnyAuthority('DEALER_MANAGER', 'ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Long id,
            @RequestBody VehicleRequest request) { 
        VehicleResponse updated = vehicleService.updateVehicle(id, request, null); 
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle updated successfully", updated));
    }

    // 🚫 DEACTIVATE vehicle (soft delete)
    @PreAuthorize("hasAnyAuthority('DEALER_MANAGER', 'ADMIN', 'EVM_STAFF')")
    @PutMapping("/deactivate/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateVehicle(@PathVariable Long id) {
        vehicleService.deactivateVehicle(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle deactivated successfully", null));
    }

    // ✅ ACTIVATE vehicle again
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @PutMapping("/activate/{id}")
    public ResponseEntity<ApiResponse<Void>> activateVehicle(@PathVariable Long id) {
        vehicleService.activateVehicle(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle activated successfully", null));
    }

    // ⚖️ COMPARE variants
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER')")
    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<List<VehicleComparisonDTO>>> compareVehicles(
            @RequestParam List<Long> variantIds) {

        List<VehicleComparisonDTO> comparisonData = vehicleService.compareVariants(variantIds);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle comparison data retrieved successfully", comparisonData));
    }
}

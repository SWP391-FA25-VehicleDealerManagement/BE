package com.example.evm.controller.vehicle;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    // Lấy thông tin xe theo ID
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    @GetMapping("/{id}") 
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable Integer id) {
    
    // Yêu cầu phải có trong VehicleService
    VehicleResponse vehicle = vehicleService.getVehicleById(id); 
    
    return ResponseEntity.ok(
        new ApiResponse<>(true, "Vehicle retrieved successfully", vehicle)
    );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<List<VehicleComparisonDTO>>> compareVehicles(
        @RequestParam List<Integer> variantIds) {
        
        List<VehicleComparisonDTO> comparisonData = vehicleService.compareVariants(variantIds);
        
        return ResponseEntity.ok(
            new ApiResponse<>(true, "Vehicle comparison data retrieved successfully", comparisonData)
        );
    }

    // Thêm xe mới (POST /api/vehicles)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Valid @RequestBody VehicleRequest request) {
        
        VehicleResponse createdVehicle = vehicleService.createVehicle(request);
        return new ResponseEntity<>(
                new ApiResponse<>(true, "Xe đã được thêm mới thành công", createdVehicle),
                HttpStatus.CREATED
        );
    }

    // Cập nhật thông tin xe (PUT /api/vehicles/{id})
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Integer id,
            @Valid @RequestBody VehicleRequest request) {
        
        VehicleResponse updatedVehicle = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Thông tin xe đã được cập nhật thành công", updatedVehicle)
        );
    }

    // Xóa xe (DELETE /api/vehicles/{id})
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Integer id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Xe đã được xóa thành công", null)
        );
    }
}

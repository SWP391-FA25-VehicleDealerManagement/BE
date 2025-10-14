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

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // 🟢 GET all active vehicles
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllActiveVehicles() {
        List<VehicleResponse> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(new ApiResponse<>(true, "Active vehicles retrieved successfully", vehicles));
    }

    // 🔴 GET all inactive vehicles
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
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

    // 🔍 SEARCH by name
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER', 'ADMIN', 'EVM_STAFF')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> searchVehiclesByName(@RequestParam String name) {
        List<VehicleResponse> vehicles = vehicleService.searchVehiclesByName(name);
        return ResponseEntity.ok(new ApiResponse<>(true, "Search completed successfully", vehicles));
    }

    // ➕ ADD new vehicle
    @PreAuthorize("hasAnyAuthority('DEALER_MANAGER', 'ADMIN', 'EVM_STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> addVehicle(@RequestBody VehicleRequest request) {
        VehicleResponse created = vehicleService.addVehicle(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle added successfully", created));
    }

    // 🔄 UPDATE existing vehicle
    @PreAuthorize("hasAnyAuthority('DEALER_MANAGER', 'ADMIN', 'EVM_STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Long id,
            @RequestBody VehicleRequest request) {

        VehicleResponse updated = vehicleService.updateVehicle(id, request);
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

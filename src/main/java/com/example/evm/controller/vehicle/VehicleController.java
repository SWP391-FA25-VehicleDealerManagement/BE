package com.example.evm.controller.vehicle;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;
import com.example.evm.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // 🔹 GET all
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'DEALER_MANAGER')")
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    // 🔹 ADD
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'DEALER_MANAGER')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> addVehicle(@RequestBody VehicleRequest request) {
        VehicleResponse created = vehicleService.addVehicle(request);
        return ResponseEntity.ok(Map.of(
                "message", "Vehicle added successfully",
                "vehicle", created
        ));
    }

    // 🔹 UPDATE
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'DEALER_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateVehicle(
            @PathVariable Long id,
            @RequestBody VehicleRequest request) {

        VehicleResponse updated = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(Map.of(
                "message", "Vehicle updated successfully",
                "vehicle", updated
        ));
    }

    // 🔹 DELETE
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'DEALER_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(Map.of("message", "Vehicle deleted successfully"));
    }

    // 🔹 COMPARE
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'DEALER_MANAGER')")
    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<List<VehicleComparisonDTO>>> compareVehicles(
        @RequestParam List<Long> variantIds) {
        
        List<VehicleComparisonDTO> comparisonData = vehicleService.compareVariants(variantIds);
        
        return ResponseEntity.ok(
            new ApiResponse<>(true, "Vehicle comparison data retrieved successfully", comparisonData)
        );
    }
}
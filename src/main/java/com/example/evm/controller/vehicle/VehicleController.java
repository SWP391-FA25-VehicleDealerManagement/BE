package com.example.evm.controller.vehicle;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleResponse;
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

    @PreAuthorize("hasRole('DEALER_MANAGER') or hasRole('DEALER_STAFF')")
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @PreAuthorize("hasRole('DEALER_MANAGER') or hasRole('DEALER_STAFF')")
    @PostMapping
    public ResponseEntity<String> createVehicle(@RequestBody String body) {
        return ResponseEntity.ok("API tạo vehicle sample");
    }

    @PreAuthorize("hasRole('DEALER_STAFF') or hasRole('DEALER_MANAGER')")
    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<List<VehicleComparisonDTO>>> compareVehicles(
        @RequestParam List<Long> variantIds) {
        
        List<VehicleComparisonDTO> comparisonData = vehicleService.compareVariants(variantIds);
        
        return ResponseEntity.ok(
            new ApiResponse<>(true, "Vehicle comparison data retrieved successfully", comparisonData)
        );
    }
}

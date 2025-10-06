package com.example.evm.controller.vehicle;

<<<<<<< HEAD
import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.vehicle.VehicleComparisonDTO;
=======
import com.example.evm.dto.vehicle.VehicleRequest;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
import com.example.evm.dto.vehicle.VehicleResponse;
import com.example.evm.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
<<<<<<< HEAD
=======
import java.util.Map;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

<<<<<<< HEAD
    @PreAuthorize("hasRole('DEALER_MANAGER') or hasRole('DEALER_STAFF')")
=======
    // 🔹 GET all
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'DEALER_MANAGER')")
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

<<<<<<< HEAD
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
=======
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
            @PathVariable Integer id,
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
    public ResponseEntity<Map<String, String>> deleteVehicle(@PathVariable Integer id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(Map.of("message", "Vehicle deleted successfully"));
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    }
}

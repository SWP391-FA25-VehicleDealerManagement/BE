package com.example.evm.controller.vehicle;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.vehicle.VehicleModelRequest;
import com.example.evm.dto.vehicle.VehicleModelResponse;
import com.example.evm.service.vehicle.VehicleModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class VehicleModelController {

    private final VehicleModelService modelService;

    // ➕ TẠO MỚI một dòng xe
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleModelResponse>> createModel(@RequestBody VehicleModelRequest request) {
        VehicleModelResponse createdModel = modelService.createModel(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Model created successfully", createdModel));
    }

    // 🟢 LẤY TẤT CẢ các dòng xe
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleModelResponse>>> getAllModels() {
        List<VehicleModelResponse> models = modelService.getAllModels();
        return ResponseEntity.ok(new ApiResponse<>(true, "Models retrieved successfully", models));
    }

    // 🟢 LẤY MỘT dòng xe theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleModelResponse>> getModelById(@PathVariable Long id) {
        VehicleModelResponse model = modelService.getModelById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Model retrieved successfully", model));
    }

    // 🔄 CẬP NHẬT một dòng xe
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleModelResponse>> updateModel(@PathVariable Long id, @RequestBody VehicleModelRequest request) {
        VehicleModelResponse updatedModel = modelService.updateModel(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Model updated successfully", updatedModel));
    }

    // ❌ XÓA một dòng xe
    @PutMapping("/deactivate/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deactivateModel(@PathVariable Long id) {
        modelService.deactivateModel(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Model deactivated successfully", null));
    }

    // ✅ ACTIVATE một dòng xe
    @PutMapping("/activate/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> activateModel(@PathVariable Long id) {
        modelService.activateModel(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Model activated successfully", null));
    }
}
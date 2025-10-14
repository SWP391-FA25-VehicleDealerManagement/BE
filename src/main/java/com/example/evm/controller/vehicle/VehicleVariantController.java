package com.example.evm.controller.vehicle;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.vehicle.VehicleVariantRequest;
import com.example.evm.dto.vehicle.VehicleVariantResponse;
import com.example.evm.service.vehicle.VehicleVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class VehicleVariantController {

    private final VehicleVariantService variantService;

    // ➕ TẠO MỚI một biến thể xe
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleVariantResponse>> createVariant(@RequestBody VehicleVariantRequest request) {
        VehicleVariantResponse createdVariant = variantService.createVariant(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant created successfully", createdVariant));
    }

    // 🟢 LẤY TẤT CẢ các biến thể
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleVariantResponse>>> getAllVariants() {
        List<VehicleVariantResponse> variants = variantService.getAllVariants();
        return ResponseEntity.ok(new ApiResponse<>(true, "Variants retrieved successfully", variants));
    }

    // 🟢 LẤY MỘT biến thể theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleVariantResponse>> getVariantById(@PathVariable Long id) {
        VehicleVariantResponse variant = variantService.getVariantById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant retrieved successfully", variant));
    }

    // 🔄 CẬP NHẬT một biến thể
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleVariantResponse>> updateVariant(@PathVariable Long id, @RequestBody VehicleVariantRequest request) {
        VehicleVariantResponse updatedVariant = variantService.updateVariant(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant updated successfully", updatedVariant));
    }

    // ❌ XÓA một biến thể
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable Long id) {
        variantService.deleteVariant(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant deleted successfully", null));
    }
}
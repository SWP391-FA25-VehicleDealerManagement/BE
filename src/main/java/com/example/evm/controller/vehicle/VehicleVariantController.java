package com.example.evm.controller.vehicle;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.vehicle.VehicleDetailRequest;
import com.example.evm.dto.vehicle.VehicleDetailResponse;
import com.example.evm.dto.vehicle.VehicleVariantRequest;
import com.example.evm.dto.vehicle.VehicleVariantResponse;
import com.example.evm.service.vehicle.VehicleVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class VehicleVariantController {

    private final VehicleVariantService variantService;

    // ➕ TẠO MỚI một biến thể xe
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleVariantResponse>> createVariant(
        @RequestPart("variant") VehicleVariantRequest request,
        @RequestPart("file") MultipartFile file) {

    VehicleVariantResponse createdVariant = variantService.createVariant(request, file);
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
    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleVariantResponse>> updateVariant(
            @PathVariable Long id,
            @RequestPart("variant") VehicleVariantRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) { // Đặt required = false để không bắt buộc phải có file

        VehicleVariantResponse updatedVariant = variantService.updateVariant(id, request, file);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant updated successfully", updatedVariant));
    }

    // 🚫 DEACTIVATE a variant (soft delete)
    @PutMapping("/deactivate/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deactivateVariant(@PathVariable Long id) {
        variantService.deactivateVariant(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant deactivated successfully", null));
    }

    // ✅ ACTIVATE a variant
    @PutMapping("/activate/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> activateVariant(@PathVariable Long id) {
        variantService.activateVariant(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant activated successfully", null));
    }

    // ➕ LẤY thông số kỹ thuật của một variant
    @GetMapping("/{variantId}/details")
    public ResponseEntity<ApiResponse<VehicleDetailResponse>> getVariantDetails(@PathVariable Long variantId) {
        VehicleDetailResponse details = variantService.getDetailsByVariantId(variantId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Details retrieved successfully", details));
    }

    // 🔄 THÊM/CẬP NHẬT thông số kỹ thuật cho một variant
    @PostMapping("/{variantId}/details")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleDetailResponse>> addOrUpdateVariantDetails(
            @PathVariable Long variantId,
            @RequestBody VehicleDetailRequest request) {
        VehicleDetailResponse details = variantService.addOrUpdateDetails(variantId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Details added/updated successfully", details));
    }
}
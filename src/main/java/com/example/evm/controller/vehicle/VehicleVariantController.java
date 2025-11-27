package com.example.evm.controller.vehicle;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.vehicle.VehicleDetailRequest;
import com.example.evm.dto.vehicle.VehicleDetailResponse;
import com.example.evm.dto.vehicle.VehicleVariantRequest;
import com.example.evm.dto.vehicle.VehicleVariantResponse;
import com.example.evm.service.vehicle.VehicleVariantService; 

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Parameter;


@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
@Slf4j
public class VehicleVariantController {

    private final VehicleVariantService variantService;

    // ➕ TẠO MỚI một biến thể xe
    @PostMapping 
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleVariantResponse>> createVariant(
            @Valid @RequestBody VehicleVariantRequest requestDto 
    ) {
        // Gọi service (Service interface phải là createVariant(VehicleVariantRequest request))
        VehicleVariantResponse createdVariant = variantService.createVariant(requestDto);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant created successfully", createdVariant));
    }

    // 🟢 LẤY TẤT CẢ các biến thể (với optional dealerId để lấy giá dealer)
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleVariantResponse>>> getAllVariants(
            @RequestParam(required = false) Long dealerId) {
        List<VehicleVariantResponse> variants = variantService.getAllVariants(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variants retrieved successfully", variants));
    }


    // 🟢 LẤY MỘT biến thể theo ID (với optional dealerId để lấy giá dealer)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleVariantResponse>> getVariantById(
            @PathVariable Long id,
            @RequestParam(required = false) Long dealerId) {
        VehicleVariantResponse variant = variantService.getVariantById(id, dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant retrieved successfully", variant));
    }

    // 🔄 CẬP NHẬT một biến thể
    @PutMapping(value = "/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleVariantResponse>> updateVariant(
            @PathVariable Long id,
            @Valid @RequestBody VehicleVariantRequest requestDto 
    ) {
        
        // Gọi service (Service interface phải là updateVariant(Long id, VehicleVariantRequest request))
        VehicleVariantResponse updatedVariant = variantService.updateVariant(id, requestDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant updated successfully", updatedVariant));
    }

    // 🚫 DEACTIVATE a variant (soft delete)
    @PutMapping("/deactivate/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deactivateVariant(@PathVariable Long id) {
        variantService.deactivateVariant(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant deactivated successfully", null));
    }

    //  ACTIVATE a variant
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

    // 🔄 THÊM thông số kỹ thuật cho một variant
    @PostMapping("/{variantId}/details")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleDetailResponse>> createDetails(
            @PathVariable Long variantId,
            @RequestBody VehicleDetailRequest request) {
        VehicleDetailResponse details = variantService.createDetails(variantId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Details created successfully", details));
    }

    // Sửa thông số kỹ thuật cho một variant
    @PutMapping("/{variantId}/details")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleDetailResponse>> updateDetails(
            @PathVariable Long variantId,
            @Valid @RequestBody VehicleDetailRequest request) {
        VehicleDetailResponse updatedDetails = variantService.updateDetails(variantId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle details updated successfully", updatedDetails));
    }

    //  XÓA HẲN một biến thể xe
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @Parameter(description = "ID của Variant cần xóa hẳn") @PathVariable Long id
    ) {
        // Service sẽ kiểm tra xem variant có đang được Vehicle sử dụng không
        variantService.deleteVariant(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant permanently deleted", null));
    }
}
package com.example.evm.controller.vehicle;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.vehicle.VehicleDetailRequest;
import com.example.evm.dto.vehicle.VehicleDetailResponse;
import com.example.evm.dto.vehicle.VehicleVariantRequest;
import com.example.evm.dto.vehicle.VehicleVariantResponse;
import com.example.evm.service.vehicle.VehicleVariantService;
import com.example.evm.service.storage.FileStorageService; 

import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;


@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
@Slf4j
public class VehicleVariantController {

    private final VehicleVariantService variantService;
    private final FileStorageService fileStorageService;

    // ➕ TẠO MỚI một biến thể xe
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleVariantResponse>> createVariant(
            @Parameter(in = ParameterIn.DEFAULT, description = "Tên phiên bản")
            @RequestParam("name") @NotBlank String name,

            @Parameter(in = ParameterIn.DEFAULT, description = "ID của Model")
            @RequestParam("modelId") @NotNull Long modelId,

            @Parameter(in = ParameterIn.DEFAULT, description = "Giá niêm yết (MSRP)")
            @RequestParam("msrp") @NotNull BigDecimal msrp,

            @Parameter(description = "File ảnh")
            @RequestParam(value = "file", required = true) MultipartFile file) {

            VehicleVariantRequest requestDto = new VehicleVariantRequest();
            requestDto.setName(name);
            requestDto.setModelId(modelId);
            requestDto.setMsrp(msrp);

        VehicleVariantResponse createdVariant = variantService.createVariant(requestDto, file);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variant created successfully", createdVariant));
    }

    // 🟢 LẤY TẤT CẢ các biến thể (với optional dealerId để lấy giá dealer)
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleVariantResponse>>> getAllVariants(
            @RequestParam(required = false) Long dealerId) {
        List<VehicleVariantResponse> variants = variantService.getAllVariants(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Variants retrieved successfully", variants));
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource file = fileStorageService.load(filename);
        String contentType = "application/octet-stream"; // Mặc định
        try {
            // Cố gắng tự động xác định ContentType từ file
            contentType = Files.probeContentType(file.getFile().toPath());
        } catch (IOException e) {
            log.error("Could not determine file type for variant image: {}", filename, e);
        }

        // Nếu không xác định được, vẫn dùng loại mặc định
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
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
    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<VehicleVariantResponse>> updateVariant(
        @Parameter(description = "ID của Variant cần cập nhật") @PathVariable Long id,

        @Parameter(in = ParameterIn.DEFAULT, description = "Tên phiên bản mới (tùy chọn)")
        @RequestParam(value = "name", required = false) String name,

        @Parameter(in = ParameterIn.DEFAULT, description = "ID Model mới (tùy chọn)")
        @RequestParam(value = "modelId", required = false) Long modelId,

        @Parameter(in = ParameterIn.DEFAULT, description = "Giá niêm yết mới (tùy chọn)")
        @RequestParam(value = "msrp", required = false) BigDecimal msrp,

        @Parameter(description = "File ảnh mới (tùy chọn)")
        @RequestParam(value = "image", required = false) MultipartFile file) { 

        VehicleVariantRequest requestDto = new VehicleVariantRequest();
        if (name != null) requestDto.setName(name);
        if (modelId != null) requestDto.setModelId(modelId);
        if (msrp != null) requestDto.setMsrp(msrp);

    VehicleVariantResponse updatedVariant = variantService.updateVariant(id, requestDto, file);
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
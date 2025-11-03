package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleDetailRequest;
import com.example.evm.dto.vehicle.VehicleDetailResponse;
import com.example.evm.dto.vehicle.VehicleVariantRequest;
import com.example.evm.dto.vehicle.VehicleVariantResponse;

import java.util.List;

public interface VehicleVariantService {
    VehicleVariantResponse createVariant(VehicleVariantRequest request);
    List<VehicleVariantResponse> getAllVariants(Long dealerId); // Thêm dealerId để lấy giá dealer
    VehicleVariantResponse getVariantById(Long id, Long dealerId); // Thêm dealerId để lấy giá dealer
    VehicleVariantResponse updateVariant(Long id, VehicleVariantRequest request);
    void deactivateVariant(Long id);
    void activateVariant(Long id);

    VehicleDetailResponse createDetails(Long variantId, VehicleDetailRequest request);
    VehicleDetailResponse updateDetails(Long variantId, VehicleDetailRequest request);
    VehicleDetailResponse getDetailsByVariantId(Long variantId);
    void deleteVariant(Long id);
}
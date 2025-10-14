package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleVariantRequest;
import com.example.evm.dto.vehicle.VehicleVariantResponse;

import java.util.List;

public interface VehicleVariantService {
    VehicleVariantResponse createVariant(VehicleVariantRequest request);
    List<VehicleVariantResponse> getAllVariants();
    VehicleVariantResponse getVariantById(Long id);
    VehicleVariantResponse updateVariant(Long id, VehicleVariantRequest request);
    void deleteVariant(Long id);
}
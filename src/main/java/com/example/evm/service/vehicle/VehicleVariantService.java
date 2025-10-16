package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleDetailRequest;
import com.example.evm.dto.vehicle.VehicleDetailResponse;
import com.example.evm.dto.vehicle.VehicleVariantRequest;
import com.example.evm.dto.vehicle.VehicleVariantResponse;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface VehicleVariantService {
    VehicleVariantResponse createVariant(VehicleVariantRequest request, MultipartFile file);
    List<VehicleVariantResponse> getAllVariants();
    VehicleVariantResponse getVariantById(Long id);
    VehicleVariantResponse updateVariant(Long id, VehicleVariantRequest request, MultipartFile file);
    void deactivateVariant(Long id);
    void activateVariant(Long id);

    VehicleDetailResponse addOrUpdateDetails(Long variantId, VehicleDetailRequest request);
    VehicleDetailResponse getDetailsByVariantId(Long variantId);
}
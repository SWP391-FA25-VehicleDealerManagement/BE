package com.example.evm.service.vehicle;

import java.util.List;

import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;

public interface VehicleService {
    List<VehicleResponse> getAllVehicles();

    VehicleResponse addVehicle(VehicleRequest request);

    VehicleResponse updateVehicle(Long id, VehicleRequest request);

    void deleteVehicle(Long id);

    List<VehicleComparisonDTO> compareVariants(List<Long> variantIds);
}
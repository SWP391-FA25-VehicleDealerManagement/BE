package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;
import java.util.List;

public interface VehicleService {
    List<VehicleResponse> getAllVehicles();
    List<VehicleComparisonDTO> compareVariants(List<Integer> variantIds);
    VehicleResponse getVehicleById(Integer vehicleId);
    VehicleResponse createVehicle(VehicleRequest request);
    VehicleResponse updateVehicle(Integer vehicleId, VehicleRequest request);
    void deleteVehicle(Integer vehicleId);
}

package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;

import java.util.List;

public interface VehicleService {

    List<VehicleResponse> getAllVehicles();
    List<VehicleResponse> getAllInactiveVehicles();
    List<VehicleResponse> searchVehiclesByName(String name);

    VehicleResponse addVehicle(VehicleRequest request);
    VehicleResponse updateVehicle(Long id, VehicleRequest request);

    void deactivateVehicle(Long id);
    void activateVehicle(Long id);

    List<VehicleComparisonDTO> compareVariants(List<Long> variantIds);
}

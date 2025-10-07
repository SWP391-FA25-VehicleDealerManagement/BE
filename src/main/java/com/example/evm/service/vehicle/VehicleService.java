package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;
import java.util.List;

public interface VehicleService {
    List<VehicleResponse> getAllVehicles();

    VehicleResponse addVehicle(VehicleRequest request);

    VehicleResponse updateVehicle(Long id, VehicleRequest request);

    void deleteVehicle(Long id);
    
    List<VehicleComparisonDTO> compareVariants(List<Long> variantIds);
}

package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleResponse;
import java.util.List;

public interface VehicleService {
    List<VehicleResponse> getAllVehicles();
    List<VehicleComparisonDTO> compareVariants(List<Integer> variantIds);
}

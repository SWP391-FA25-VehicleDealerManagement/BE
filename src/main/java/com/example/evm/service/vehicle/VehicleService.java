package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleComparisonDTO;
import com.example.evm.dto.vehicle.VehicleRequest;
import com.example.evm.dto.vehicle.VehicleResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VehicleService {

    List<VehicleResponse> getAllVehicles();
    List<VehicleResponse> getAllInactiveVehicles();
    VehicleResponse getVehicleById(Long id);
    List<VehicleResponse> searchVehiclesByName(String name);

    VehicleResponse addVehicle(VehicleRequest request, MultipartFile file);
    VehicleResponse updateVehicle(Long id, VehicleRequest request, MultipartFile file);

    void deactivateVehicle(Long id);
    void activateVehicle(Long id);

    List<VehicleComparisonDTO> compareVariants(List<Long> variantIds);

    List<VehicleResponse> getVehiclesByDealerId(Long dealerId);
}

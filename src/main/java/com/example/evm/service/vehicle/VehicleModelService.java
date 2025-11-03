package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.VehicleModelRequest;
import com.example.evm.dto.vehicle.VehicleModelResponse;
import java.util.List;

public interface VehicleModelService {
    VehicleModelResponse createModel(VehicleModelRequest request);
    List<VehicleModelResponse> getAllModels();
    VehicleModelResponse getModelById(Long id);
    VehicleModelResponse updateModel(Long id, VehicleModelRequest request);
    void deactivateModel(Long id);
    void activateModel(Long id);
    void deleteModel(Long id);
}
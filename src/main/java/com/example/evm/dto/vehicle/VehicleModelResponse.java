package com.example.evm.dto.vehicle;

import com.example.evm.entity.vehicle.VehicleModel;
import lombok.Data;

@Data
public class VehicleModelResponse {
    private Long modelId;
    private String name;
    private String description;

    public VehicleModelResponse(VehicleModel model) {
        this.modelId = model.getModelId();
        this.name = model.getName();
        this.description = model.getDescription();
    }
}
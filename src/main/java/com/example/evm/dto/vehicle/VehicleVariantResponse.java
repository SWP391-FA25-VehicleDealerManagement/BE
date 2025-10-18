package com.example.evm.dto.vehicle;

import com.example.evm.entity.vehicle.VehicleVariant;
import lombok.Data;

@Data
public class VehicleVariantResponse {
    private Long variantId;
    private String name;
    private String image;
    private Long modelId;
    private String modelName;

    public VehicleVariantResponse(VehicleVariant variant) {
        this.variantId = variant.getVariantId();
        this.name = variant.getName();
        this.image = variant.getImage();
        if (variant.getModel() != null) {
            this.modelId = variant.getModel().getModelId();
            this.modelName = variant.getModel().getName();
        }
    }
}
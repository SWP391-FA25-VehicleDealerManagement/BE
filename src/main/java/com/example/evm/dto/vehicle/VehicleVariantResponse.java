package com.example.evm.dto.vehicle;

import com.example.evm.entity.vehicle.VehicleVariant;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "variantId", "modelId", "modelName", "name", "msrp", "status", "defaultImageUrl" })
@Data
@NoArgsConstructor
public class VehicleVariantResponse {
    private Long variantId;
    private Long modelId;
    private String name;
    private String defaultImageUrl;
    private String status;
    private BigDecimal msrp;
    private String modelName;

    public VehicleVariantResponse(VehicleVariant variant) {
        this.variantId = variant.getVariantId();
        this.name = variant.getName();
        this.defaultImageUrl = variant.getImageUrl();
        this.status = variant.getStatus();
        this.msrp = variant.getMsrp();
        if (variant.getModel() != null) {
            this.modelId = variant.getModel().getModelId();
            this.modelName = variant.getModel().getName();
        }
    }
}
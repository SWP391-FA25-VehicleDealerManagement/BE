package com.example.evm.dto.vehicle;

import com.example.evm.entity.vehicle.VehicleVariant;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class VehicleVariantResponse {
    private Long variantId;
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
            this.modelName = variant.getModel().getName();
        }
    }
}
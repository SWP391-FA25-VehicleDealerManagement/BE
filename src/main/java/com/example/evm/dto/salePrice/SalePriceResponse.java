package com.example.evm.dto.salePrice;

import com.example.evm.entity.salePrice.SalePrice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalePriceResponse {
    private Long salepriceId;
    private Long dealerId;
    private Long variantId;
    private String variantName;
    private Long modelId;
    private String modelName;
    private BigDecimal basePrice;
    private BigDecimal price;
    private LocalDate effectiveDate;

    public SalePriceResponse(SalePrice salePrice) {
        this.salepriceId = salePrice.getSalepriceId();
        this.dealerId = salePrice.getDealerId();
        this.variantId = salePrice.getVariantId();
        this.basePrice = salePrice.getBasePrice();
        this.price = salePrice.getPrice();
        this.effectiveDate = salePrice.getEffectiveDate();
        
        // Lấy thông tin variant và model
        if (salePrice.getVariant() != null) {
            this.variantName = salePrice.getVariant().getName();
            if (salePrice.getVariant().getModel() != null) {
                this.modelId = salePrice.getVariant().getModel().getModelId();
                this.modelName = salePrice.getVariant().getModel().getName();
            }
        }
    }
}


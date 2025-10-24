package com.example.evm.dto.vehicle;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class VehicleComparisonDTO {
    // Thông tin cơ bản về biến thể
    private Long variantId;
    private String variantName;
    
    // Thông tin về mẫu xe
    private Long modelId;
    private String modelName;
    private String modelDescription;

    // Thông tin về giá bán
    private BigDecimal price;
    private Long dealerId;
    private String effectiveDate;

    private String variantImage;
}
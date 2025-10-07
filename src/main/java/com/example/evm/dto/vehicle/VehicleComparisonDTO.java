package com.example.evm.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    
    // Additional fields for comparison
    private String engineType;
    private String transmission;
    private String fuelType;
    private Integer seatingCapacity;
    private Integer discountPercentage;
}
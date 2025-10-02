package com.example.evm.dto.vehicle;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VehicleComparisonDTO {
    // Thông tin cơ bản về biến thể
    private Integer variantId;
    private String variantName;
    
    // Thông tin về mẫu xe
    private Integer modelId;
    private String modelName;
    private String modelDescription;

    // Thông tin về giá bán
    private BigDecimal price;
    private Integer dealerId;
    private String effectiveDate; // Để dễ đọc hơn
}
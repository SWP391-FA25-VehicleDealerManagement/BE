package com.example.evm.dto.vehicle;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VehicleComparisonDTO {
    // Thông tin cơ bản về biến thể
<<<<<<< HEAD
    private Long variantId;
    private String variantName;
    
    // Thông tin về mẫu xe
    private Long modelId;
=======
    private Integer variantId;
    private String variantName;
    
    // Thông tin về mẫu xe
    private Integer modelId;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    private String modelName;
    private String modelDescription;

    // Thông tin về giá bán
    private BigDecimal price;
<<<<<<< HEAD
    private Long dealerId;
=======
    private Integer dealerId;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    private String effectiveDate;

    private String variantImage;
}
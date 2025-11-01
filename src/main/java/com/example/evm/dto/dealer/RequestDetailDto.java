package com.example.evm.dto.dealer;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDetailDto {
    @NotNull(message = "Variant ID is required")
    private Long variantId;
    
    @NotNull(message = "Color is required")
    private String color;  // ✅ Thêm trường màu sắc
    
    @NotNull(message = "Quantity is required")
    private Integer quantity;
    
    @NotNull(message = "Unit price is required")
    private BigDecimal unitPrice;
    
    private String notes;
}


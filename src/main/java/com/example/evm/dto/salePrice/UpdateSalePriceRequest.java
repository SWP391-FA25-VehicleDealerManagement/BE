package com.example.evm.dto.salePrice;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để update sale price
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSalePriceRequest {
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    private LocalDate effectiveDate;
}


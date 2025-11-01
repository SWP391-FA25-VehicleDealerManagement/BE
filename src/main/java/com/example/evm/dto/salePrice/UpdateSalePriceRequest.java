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
    
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;  // Giá gốc hãng (optional update)
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;  // Giá bán dealer (optional update)
    
    private LocalDate effectiveDate;
}


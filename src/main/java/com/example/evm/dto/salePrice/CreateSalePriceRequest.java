package com.example.evm.dto.salePrice;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để tạo sale price mới
 *  KHÔNG bao gồm salepriceId - để database tự động tạo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSalePriceRequest {
    
    @NotNull(message = "Dealer ID is required")
    private Long dealerId;
    
    @NotNull(message = "Variant ID is required")
    private Long variantId;
    
    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;  // Giá gốc hãng
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;  // Giá bán dealer
    
    private LocalDate effectiveDate;
}


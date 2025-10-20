package com.example.evm.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class StockRequest {

    @NotNull(message = "Variant ID is required")
    private Long variantId;

    @NotNull(message = "Dealer ID is required")
    private Long dealerId;

    @NotBlank(message = "Color is required")
    private String color;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Listing price is required")
    private BigDecimal listingPrice;

    @NotBlank(message = "Status is required")
    private String status;
}
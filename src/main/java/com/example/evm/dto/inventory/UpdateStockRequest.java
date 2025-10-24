package com.example.evm.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO dùng riêng cho việc CẬP NHẬT số lượng/giá/trạng thái kho
 */
@Data
@NoArgsConstructor
public class UpdateStockRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
    private String color;
    private BigDecimal listingPrice;
    private String status;
}
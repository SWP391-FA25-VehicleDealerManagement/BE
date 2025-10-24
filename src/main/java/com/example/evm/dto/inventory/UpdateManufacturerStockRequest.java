package com.example.evm.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dùng riêng cho việc CẬP NHẬT số lượng/trạng thái KHO TỔNG
 */
@Data
@NoArgsConstructor
public class UpdateManufacturerStockRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
    private String color;
    private String status;
}
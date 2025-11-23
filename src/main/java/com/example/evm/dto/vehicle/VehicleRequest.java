package com.example.evm.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO VehicleRequest - Request để tạo xe mới
 * 
 * Lưu ý:
 * - Không cần tạo VehicleDetail (đã có sẵn trong VehicleVariant)
 * - Chỉ cần variantId và color
 * - VIN number sẽ được tự động generate
 */
@Data
public class VehicleRequest {
    
    @NotNull(message = "VIN Number is required")
    private String vinNumber;

    @NotNull(message = "Variant ID is required")
    private Long variantId;
    
    @NotBlank(message = "Color is required")
    private String color;

    private Boolean testDrive;
}

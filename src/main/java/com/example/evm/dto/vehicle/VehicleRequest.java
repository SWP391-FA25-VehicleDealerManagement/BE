package com.example.evm.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal; // Nếu bạn có trường giá trực tiếp trong Vehicle

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    // Thông tin cơ bản của xe
    @NotBlank(message = "Tên xe không được để trống")
    private String name;
    
    @NotBlank(message = "Màu xe không được để trống")
    private String color;
    
    private String image; // URL/Path ảnh

    // Foreign Key: Liên kết với VehicleVariant
    @NotNull(message = "Variant ID không được để trống")
    private Integer variantId;
    
    // Foreign Key: Liên kết với Dealer
    @NotNull(message = "Dealer ID không được để trống")
    private Integer dealerId;
    
    // Thêm các trường khác của Vehicle Entity nếu có (ví dụ: VIN)
}
package com.example.evm.dto.vehicle;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO VehicleFullResponse - Response đầy đủ thông tin xe
 * 
 * Bao gồm:
 * - Thông tin Vehicle (vehicleId, VIN, color, status, dates)
 * - Thông tin VehicleVariant (variantId, name, image, msrp)
 * - Thông tin VehicleModel (modelId, name, manufacturer, year, bodyType)
 * - Thông tin VehicleDetail (thông số kỹ thuật đầy đủ)
 */
@Data
@Builder
public class VehicleFullResponse {
    
    // ===== VEHICLE INFO =====
    private Long vehicleId;
    private String vinNumber;
    private String color;
    private String imageUrl;
    private String status;
    private LocalDate manufactureDate;
    private LocalDate warrantyExpiryDate;
    
    // ===== VARIANT INFO =====
    private Long variantId;
    private String variantName;
    private BigDecimal msrp;
    
    // ===== MODEL INFO =====
    private Long modelId;
    private String modelName;
    private String manufacturer;
    private Integer year;
    private String bodyType;
    
    // ===== VEHICLE DETAIL (Thông số kỹ thuật) =====
    private VehicleDetailResponse detail;
    
}


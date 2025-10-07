package com.example.evm.dto.vehicle;

import lombok.Data;

@Data
public class VehicleRequest {
    private String name;
    private String color;
    private String image;
    private Double price;
    private Integer stock;
    private Long dealerId;
    private Long variantId;
    
    // Additional fields for compatibility
    private String vehicleName;
    private String vehicleType;
    private String description;
}

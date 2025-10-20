package com.example.evm.dto.vehicle;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VehicleRequest {
    
    private String name;
    private String color;
    private String image;
    private Double price;
    private Integer stock;
    private Long dealerId;
    private Long variantId;
    
    private String vehicleName;
    private String vehicleType;
    private String description;
    
    private Long stockId; 
    
    private String vinNumber;
    private String licensePlate;
    private LocalDate manufactureDate;
    private LocalDate warrantyExpiryDate;
}
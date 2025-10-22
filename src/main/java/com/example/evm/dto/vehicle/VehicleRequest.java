package com.example.evm.dto.vehicle;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VehicleRequest {
    
    private String color;
    private Long variantId;
    private Long stockId; 
    
    private String vinNumber;
    private LocalDate manufactureDate;
    private LocalDate warrantyExpiryDate;
}
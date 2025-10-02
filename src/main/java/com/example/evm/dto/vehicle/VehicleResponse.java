package com.example.evm.dto.vehicle;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VehicleResponse {
    private Integer vehicleId;
    private String name;
    private String color;
    private String variantName;
    private String modelName;
    private String modelDescription;
    private String price;
    private String dealerName;
    private String vehicleImage;   // 🆕 image của Vehicle
    private String variantImage;   // 🆕 image của Variant
}

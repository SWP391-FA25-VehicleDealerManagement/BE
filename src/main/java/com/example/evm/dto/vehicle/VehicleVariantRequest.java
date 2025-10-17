package com.example.evm.dto.vehicle;

import lombok.Data;

@Data
public class VehicleVariantRequest {
    private String name;
    private String image;
    private Long modelId;
}
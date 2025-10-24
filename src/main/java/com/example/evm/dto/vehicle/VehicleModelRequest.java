package com.example.evm.dto.vehicle;

import lombok.Data;

@Data
public class VehicleModelRequest {
    private String name;
    private String description;
    private String manufacturer;
    private Integer year;
    private String body_type;
}
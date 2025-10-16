package com.example.evm.dto.vehicle;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VehicleDetailRequest {
    // --- Thông số ---
    private String dimensionsMm;
    private Integer wheelbaseMm;
    private Integer groundClearanceMm;
    private Integer curbWeightKg;
    private Integer seatingCapacity;
    private Integer trunkCapacityLiters;

    // --- Động cơ & Vận Hành ---
    private String engineType;
    private String maxPower;
    private String maxTorque;
    private Integer topSpeedKmh;
    private String drivetrain;
    private String driveModes;

    // --- Pin & Khả năng di chuyển ---
    private BigDecimal batteryCapacityKwh;
    private Integer rangePerChargeKm;
    private String chargingTime;

    // --- Thiết kế ---
    private String exteriorFeatures;
    private String interiorFeatures;

    // --- Tính năng an toàn ---
    private String airbags;
    private String brakingSystem;
    private Boolean hasEsc;
    private Boolean hasHillStartAssist;
    private Boolean hasTpms;
    private Boolean hasRearCamera;
    private Boolean hasChildLock;
}
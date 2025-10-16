package com.example.evm.dto.vehicle;

import com.example.evm.entity.vehicle.VehicleDetail;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class VehicleDetailResponse {

    // --- BƯỚC 1: KHAI BÁO TẤT CẢ CÁC THUỘC TÍNH (FIELDS) ---
    private Long detailId;
    private Long variantId;

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


    // --- BƯỚC 2: CONSTRUCTOR ĐỂ MAP DỮ LIỆU (ĐOẠN CODE CỦA BẠN) ---
    public VehicleDetailResponse(VehicleDetail detail) {
        this.detailId = detail.getDetailId();
        if (detail.getVariant() != null) {
            this.variantId = detail.getVariant().getVariantId();
        }

        // --- Thông số ---
        this.dimensionsMm = detail.getDimensionsMm();
        this.wheelbaseMm = detail.getWheelbaseMm();
        this.groundClearanceMm = detail.getGroundClearanceMm();
        this.curbWeightKg = detail.getCurbWeightKg();
        this.seatingCapacity = detail.getSeatingCapacity();
        this.trunkCapacityLiters = detail.getTrunkCapacityLiters();

        // --- Động cơ & Vận Hành ---
        this.engineType = detail.getEngineType();
        this.maxPower = detail.getMaxPower();
        this.maxTorque = detail.getMaxTorque();
        this.topSpeedKmh = detail.getTopSpeedKmh();
        this.drivetrain = detail.getDrivetrain();
        this.driveModes = detail.getDriveModes();

        // --- Pin & Khả năng di chuyển ---
        this.batteryCapacityKwh = detail.getBatteryCapacityKwh();
        this.rangePerChargeKm = detail.getRangePerChargeKm();
        this.chargingTime = detail.getChargingTime();

        // --- Thiết kế ---
        this.exteriorFeatures = detail.getExteriorFeatures();
        this.interiorFeatures = detail.getInteriorFeatures();

        // --- Tính năng an toàn ---
        this.airbags = detail.getAirbags();
        this.brakingSystem = detail.getBrakingSystem();
        this.hasEsc = detail.getHasEsc();
        this.hasHillStartAssist = detail.getHasHillStartAssist();
        this.hasTpms = detail.getHasTpms();
        this.hasRearCamera = detail.getHasRearCamera();
        this.hasChildLock = detail.getHasChildLock();
    }
}
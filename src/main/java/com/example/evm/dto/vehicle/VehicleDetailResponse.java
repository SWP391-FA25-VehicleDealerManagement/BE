package com.example.evm.dto.vehicle;

import com.example.evm.entity.vehicle.VehicleDetail;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VehicleDetailResponse {
    
    private String dimensionsMm;
    private Integer wheelbaseMm;
    private Integer groundClearanceMm;
    private Integer curbWeightKg;
    private Integer seatingCapacity;
    private Integer trunkCapacityLiters;
    private String engineType;
    private String maxPower;
    private Integer topSpeedKmh;
    private String drivetrain;
    private String driveModes;
    private java.math.BigDecimal batteryCapacityKwh;
    private Integer rangePerChargeKm;
    private String chargingTime;
    private String exteriorFeatures;
    private String interiorFeatures;
    private Integer airbags;
    private String brakingSystem;
    private Boolean hasEsc;
    private Boolean hasTpms;
    private Boolean hasRearCamera;
    private Boolean hasChildLock;

    public VehicleDetailResponse(VehicleDetail detail) {
        this.dimensionsMm = detail.getDimensionsMm();
        
        try {
            this.wheelbaseMm = parseInteger(detail.getWheelbaseMm());
            this.groundClearanceMm = parseInteger(detail.getGroundClearanceMm());
            this.curbWeightKg = parseInteger(detail.getCurbWeightKg());
            this.seatingCapacity = parseInteger(detail.getSeatingCapacity());
            this.trunkCapacityLiters = parseInteger(detail.getTrunkCapacityLiters());
            this.topSpeedKmh = parseInteger(detail.getTopSpeedKmh());
            this.rangePerChargeKm = parseInteger(detail.getRangePerChargeKm());
            this.airbags = parseInteger(detail.getAirbags());
        } catch (Exception e) {
        }

        this.engineType = detail.getEngineType();
        this.maxPower = detail.getMaxPower();
        this.drivetrain = detail.getDrivetrain();
        this.driveModes = detail.getDriveModes();
        this.batteryCapacityKwh = detail.getBatteryCapacityKwh();
        this.chargingTime = detail.getChargingTime();
        this.exteriorFeatures = detail.getExteriorFeatures();
        this.interiorFeatures = detail.getInteriorFeatures();
        this.brakingSystem = detail.getBrakingSystem();
        this.hasEsc = detail.getHasEsc();
        this.hasTpms = detail.getHasTpms();
        this.hasRearCamera = detail.getHasRearCamera();
        this.hasChildLock = detail.getHasChildLock();
    }
    
    // 🧩 HÀM HỖ TRỢ CHUYỂN ĐỔI KIỂU DỮ LIỆU
    private Integer parseInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
package com.example.evm.entity.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "VehicleDetail")
@Getter
@Setter
public class VehicleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    // --- Mối quan hệ một-một ---
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", referencedColumnName = "variant_id")
    @JsonIgnore // Tránh vòng lặp vô tận khi chuyển thành JSON
    private VehicleVariant variant;

    // --- Thông số ---
    @Column(name = "dimensions_mm")
    private String dimensionsMm;

    @Column(name = "wheelbase_mm")
    private Integer wheelbaseMm;

    @Column(name = "ground_clearance_mm")
    private Integer groundClearanceMm;

    @Column(name = "curb_weight_kg")
    private Integer curbWeightKg;

    @Column(name = "seating_capacity")
    private Integer seatingCapacity;

    @Column(name = "trunk_capacity_liters")
    private Integer trunkCapacityLiters;

    // --- Động cơ & Vận Hành ---
    @Column(name = "engine_type")
    private String engineType;

    @Column(name = "max_power")
    private String maxPower;

    @Column(name = "top_speed_kmh")
    private Integer topSpeedKmh;

    private String drivetrain;

    @Column(name = "drive_modes")
    private String driveModes;

    // --- Pin & Khả năng di chuyển ---
    @Column(name = "battery_capacity_kwh")
    private BigDecimal batteryCapacityKwh;

    @Column(name = "range_per_charge_km")
    private Integer rangePerChargeKm;

    @Column(name = "charging_time")
    private String chargingTime;

    // --- Thiết kế ---
    @Column(name = "exterior_features", columnDefinition = "NVARCHAR(MAX)")
    private String exteriorFeatures;

    @Column(name = "interior_features", columnDefinition = "NVARCHAR(MAX)")
    private String interiorFeatures;

    // --- Tính năng an toàn ---
    private String airbags;

    @Column(name = "braking_system")
    private String brakingSystem;

    @Column(name = "has_esc")
    private Boolean hasEsc;

    @Column(name = "has_tpms")
    private Boolean hasTpms;

    @Column(name = "has_rear_camera")
    private Boolean hasRearCamera;

    @Column(name = "has_child_lock")
    private Boolean hasChildLock;
}
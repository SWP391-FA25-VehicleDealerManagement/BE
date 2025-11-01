package com.example.evm.entity.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * Entity VehicleVariant - Đại diện cho phiên bản xe (variant)
 * 
 * Quan hệ:
 * - VehicleVariant (N) ──> (1) VehicleModel
 * - VehicleVariant (1) ──< (1) VehicleDetail (thông số kỹ thuật)
 * - VehicleVariant (1) ──< (N) Vehicle (nhiều xe cùng variant)
 */
@Entity
@Table(name = "VehicleVariant")
@Data
@NoArgsConstructor
public class VehicleVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;

    @Column(name = "name", nullable = false, length = 150)
    private String name; // Ví dụ: "VF 8 Eco", "VF 9 Plus"

    @Column(name = "status", length = 50)
    private String status; // "ACTIVE", "INACTIVE" (soft delete)

    @Column(name = "msrp", nullable = false) 
    private BigDecimal msrp; // Giá niêm yết (Manufacturer's Suggested Retail Price)

    // ===== QUAN HỆ 1-N VỚI VEHICLE =====
    // One Variant has many Vehicles
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("variant")
    private List<Vehicle> vehicles;

    // ===== QUAN HỆ 1-1 VỚI VEHICLEDETAIL =====
    // Một variant có một bộ thông số kỹ thuật
    @OneToOne(mappedBy = "variant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private VehicleDetail detail;
}

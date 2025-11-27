package com.example.evm.entity.vehicle;

import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.entity.inventory.ManufacturerStock;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Entity Vehicle - Đại diện cho từng chiếc xe riêng lẻ với VIN number
 * 
 * Quan hệ:
 * - Vehicle (N) → (1) VehicleVariant → (1) VehicleModel
 * - Vehicle (N) → (1) VehicleVariant → (1) VehicleDetail
 * - Vehicle (N) → (1) ManufacturerStock (kho tổng) hoặc (1) InventoryStock (kho dealer)
 * 
 * Lưu ý: Mỗi xe chỉ thuộc 1 kho tại 1 thời điểm (hoặc manufacturer, hoặc dealer, hoặc đã bán)
 */
@Entity
@Table(name = "Vehicle")
@Data
@NoArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vin_number", unique = true, nullable = false, length = 100)
    private String vinNumber;

    // ===== QUAN HỆ VỚI VARIANT =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    @JsonIgnore  // Tránh lazy loading issues, dùng DTO để trả về
    private VehicleVariant variant;

    // ===== THÔNG TIN XE =====
    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Column(name = "image", length = 500) 
    private String imageUrl; // Hình ảnh xe

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;

    @Column(name = "status", length = 50)
    private String status; // "IN_MANUFACTURER_STOCK", "IN_DEALER_STOCK", "SOLD", "IN_TRANSIT"

    // ===== VỊ TRÍ KHO =====
    // Xe đang ở kho tổng (nullable - nếu null thì xe không ở kho tổng)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_stock_id")
    @JsonIgnore // Tránh serialize Hibernate proxy
    private ManufacturerStock manufacturerStock;

    // Xe đang ở kho dealer (nullable - nếu null thì xe không ở kho dealer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_stock_id")
    @JsonIgnore // Tránh serialize Hibernate proxy
    private InventoryStock inventoryStock;
    
    /**
     * Constraint: Xe chỉ ở 1 nơi tại 1 thời điểm
     * - (manufacturer_stock_id IS NOT NULL AND inventory_stock_id IS NULL) hoặc
     * - (manufacturer_stock_id IS NULL AND inventory_stock_id IS NOT NULL) hoặc
     * - (manufacturer_stock_id IS NULL AND inventory_stock_id IS NULL AND status = 'SOLD')
     */
}
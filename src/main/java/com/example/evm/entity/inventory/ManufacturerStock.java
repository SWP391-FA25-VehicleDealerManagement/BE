package com.example.evm.entity.inventory; // Hoặc package entity của bạn

import com.example.evm.entity.vehicle.VehicleVariant;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ManufacturerStock") // Tên bảng trong DB
@Data
@NoArgsConstructor
public class ManufacturerStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manufacturer_stock_id")
    private Long manufacturerStockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private VehicleVariant variant;

    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    // (Nếu bạn đã thêm ràng buộc UNIQUE(variant_id, color) trong DB,
    // bạn có thể thêm @Table(uniqueConstraints=...) ở trên)
}
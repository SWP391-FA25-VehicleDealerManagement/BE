package com.example.evm.entity.inventory;

import com.example.evm.entity.vehicle.Vehicle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Entity ManufacturerStock - Đại diện cho kho tổng của nhà sản xuất
 * 
 * Quan hệ:
 * - ManufacturerStock (1) ──< (N) Vehicle (nhiều xe trong kho)
 * 
 * Lưu ý: Không lưu variant_id, color, quantity nữa
 * Thay vào đó, số lượng xe được tính bằng COUNT(Vehicle) WHERE manufacturer_stock_id = ?
 */
@Entity
@Table(name = "ManufacturerStock")
@Data
@NoArgsConstructor
public class ManufacturerStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manufacturer_stock_id")
    private Long manufacturerStockId;

    @Column(name = "warehouse_name", nullable = false, length = 100)
    private String warehouseName; // Ví dụ: "Kho Tổng VinFast"

    @Column(name = "location", length = 255)
    private String location; // Địa chỉ kho: "Hải Phòng, Việt Nam"

    @Column(name = "status", length = 50)
    private String status; // "ACTIVE", "CLOSED"

    // ===== QUAN HỆ 1-N VỚI VEHICLE =====
    // Một kho có nhiều xe
    @OneToMany(mappedBy = "manufacturerStock", fetch = FetchType.LAZY)
    @JsonIgnore // Tránh vòng lặp JSON
    private List<Vehicle> vehicles;
}
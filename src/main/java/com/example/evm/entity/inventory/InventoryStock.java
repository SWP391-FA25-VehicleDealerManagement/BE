package com.example.evm.entity.inventory;

import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.Vehicle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * Entity InventoryStock - Đại diện cho kho của dealer
 * 
 * Quan hệ:
 * - InventoryStock (1) ──< (N) Vehicle (nhiều xe trong kho dealer)
 * - InventoryStock (N) ──> (1) Dealer
 * 
 * Lưu ý: Không lưu variant_id, color, quantity, listing_price nữa
 * Thay vào đó, số lượng xe được tính bằng COUNT(Vehicle) WHERE inventory_stock_id = ?
 */
@Entity
@Table(name = "InventoryStock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long stockId;

    // Kho này thuộc dealer nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @Column(name = "status", length = 50)
    private String status; // "ACTIVE", "CLOSED"

    // ===== QUAN HỆ 1-N VỚI VEHICLE =====
    // Một kho dealer có nhiều xe
    @OneToMany(mappedBy = "inventoryStock", fetch = FetchType.LAZY)
    @JsonIgnore // Tránh vòng lặp JSON
    private List<Vehicle> vehicles;
}

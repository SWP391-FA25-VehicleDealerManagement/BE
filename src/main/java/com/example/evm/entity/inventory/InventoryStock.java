package com.example.evm.entity.inventory;

import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.*;

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
    private Integer stockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    private Dealer dealer; // null = kho tổng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", length = 50)
    private String status;
}

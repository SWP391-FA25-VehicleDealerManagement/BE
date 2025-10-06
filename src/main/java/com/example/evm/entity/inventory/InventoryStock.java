package com.example.evm.entity.inventory;

import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.entity.dealer.Dealer;
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
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    private Integer quantity;
    private String status;
}

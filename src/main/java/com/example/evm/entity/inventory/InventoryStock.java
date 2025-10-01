package com.example.evm.entity.inventory;

import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.entity.dealer.Dealer;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "InventoryStock")
public class InventoryStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stock_id;

    private Integer quantity;
    private String status;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;
}

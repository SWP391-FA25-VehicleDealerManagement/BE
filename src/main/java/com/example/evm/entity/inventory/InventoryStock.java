package com.example.evm.model.inventory;

import com.example.evm.model.vehicle.Vehicle;
import com.example.evm.model.dealer.Dealer;

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

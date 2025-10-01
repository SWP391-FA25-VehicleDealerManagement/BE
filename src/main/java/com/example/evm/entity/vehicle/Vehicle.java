package com.example.evm.model.vehicle;

import com.example.evm.model.dealer.Dealer;
import com.example.evm.model.inventory.InventoryStock;
import com.example.evm.model.promotion.PromotionVehicle;
import com.example.evm.model.inventory.VehicleModel;
import com.example.evm.model.inventory.VehicleVariant;
import com.example.evm.model.order.OrderDetail;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Vehicle")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer vehicle_id;

    private String name;
    private String color;
    private String image;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private VehicleVariant variant;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @OneToMany(mappedBy = "vehicle")
    private List<InventoryStock> inventoryStocks;

    @OneToMany(mappedBy = "vehicle")
    private List<PromotionVehicle> promotionVehicles;

    @OneToMany(mappedBy = "vehicle")
    private List<OrderDetail> orderDetails;
}

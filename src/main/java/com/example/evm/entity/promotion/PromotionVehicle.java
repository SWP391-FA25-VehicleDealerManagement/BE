package com.example.evm.model.promotion;

import com.example.evm.model.vehicle.Vehicle;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PromotionVehicle")
public class PromotionVehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer promotvehicle_id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "promo_id")
    private Promotions promo;
}

package com.example.evm.entity.inventory;

import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.entity.order.SalePrice;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "VehicleVariant")
public class VehicleVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer variant_id;

    private String name;
    private String image;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private VehicleModel model;

    @OneToMany(mappedBy = "variant")
    private List<Vehicle> vehicles;

    @OneToMany(mappedBy = "variant")
    private List<SalePrice> salePrices;
}

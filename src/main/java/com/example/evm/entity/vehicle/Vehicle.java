package com.example.evm.entity.vehicle;

import com.example.evm.entity.dealer.Dealer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Vehicle")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
<<<<<<< HEAD
    private Long vehicleId;
=======
    private Integer vehicleId;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

    @Column(nullable = false)
    private String name;
    private String color;
    private String image;
<<<<<<< HEAD
=======
    private Double price;
    private Integer stock;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

    // Vehicle belongs to one Variant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "vehicles"})
    private VehicleVariant variant;

    // Vehicle belongs to one Dealer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "vehicles"})
    private Dealer dealer;
}

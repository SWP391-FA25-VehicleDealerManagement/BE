package com.example.evm.entity.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "VehicleVariant")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VehicleVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;

    private String name;
    private String image;   
    
    // Additional fields for comparison
    private String variantName;
    private String engineType;
    private String transmission;
    private String fuelType;
    private Integer seatingCapacity;

    // Variant belongs to one Model
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "variants"})
    private VehicleModel model;

    // One Variant has many Vehicles
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("variant")
    private List<Vehicle> vehicles;
}

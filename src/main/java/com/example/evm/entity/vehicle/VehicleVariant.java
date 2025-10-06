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
<<<<<<< HEAD
    private Long variantId;
=======
    private Integer variantId;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

    private String name;
    private String image;   

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

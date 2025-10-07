package com.example.evm.entity.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "VehicleVariant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image")
    private String image;

    // Variant belongs to one Model
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "variants" })
    private VehicleModel model;

    // One Variant has many Vehicles
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("variant")
    private List<Vehicle> vehicles;
}
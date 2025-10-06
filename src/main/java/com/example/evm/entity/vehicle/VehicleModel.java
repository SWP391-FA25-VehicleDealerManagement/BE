package com.example.evm.entity.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "vehicleModel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VehicleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
<<<<<<< HEAD
    private Long modelId;
=======
    private Integer modelId;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

    private String name;
    private String description;

    // One Model has many Variants
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("model")
    private List<VehicleVariant> variants;
}

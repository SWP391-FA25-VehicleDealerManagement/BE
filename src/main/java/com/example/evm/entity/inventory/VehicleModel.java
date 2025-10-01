package com.example.evm.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "VehicleModel")
public class VehicleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer model_id;

    private String name;
    private String description;

    @OneToMany(mappedBy = "model")
    private List<VehicleVariant> variants;
}

package com.example.evm.entity.order;

import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.inventory.VehicleVariant;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SalePrice")
public class SalePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer saleprice_id;

    private Double price;
    private LocalDate effectivedate;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private VehicleVariant variant;
}

package com.example.evm.entity.inventory;

import java.math.BigDecimal;

import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.VehicleVariant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "InventoryStock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long stockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private VehicleVariant variant;

    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Column(name = "listing_price", nullable = false)
    private BigDecimal listingPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", length = 50)
    private String status;
}

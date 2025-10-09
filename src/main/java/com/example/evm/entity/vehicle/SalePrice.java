package com.example.evm.entity.vehicle;

import com.example.evm.entity.dealer.Dealer;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

import java.time.LocalDate;

@Entity
@Table(name = "SalePrice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saleprice_id")
    private Long salepriceId;

    // --- Mối quan hệ với Dealer ---
    @Column(name = "dealer_id", nullable = false)
    private Long dealerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", insertable = false, updatable = false)
    private Dealer dealer; 

    // --- Mối quan hệ với VehicleVariant ---
    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", insertable = false, updatable = false)
    private VehicleVariant variant;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "effectivedate", nullable = false)
    private LocalDate effectiveDate;
    
}
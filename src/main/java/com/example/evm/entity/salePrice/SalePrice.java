package com.example.evm.entity.salePrice;

import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    @JsonIgnore  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", insertable = false, updatable = false)
    private Dealer dealer; 

    // --- Mối quan hệ với VehicleVariant ---
    @Column(name = "variant_id", nullable = false)
    private Long variantId;

     @JsonIgnoreProperties({
        "vehicles", 
        "detail", 
        "hibernateLazyInitializer",  
        "handler"  
    })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", insertable = false, updatable = false)
    private VehicleVariant variant;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;  // Giá gốc của hãng (manufacturer price)

    @Column(name = "price", nullable = false)
    private BigDecimal price;  // Giá bán của dealer (dealer selling price)

    @Column(name = "effectivedate", nullable = false)
    private LocalDate effectiveDate;
    
}
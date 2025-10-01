package com.example.evm.entity.promotion;

import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.order.OrderDetail;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Promotions")
public class Promotions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer promo_id;

    private String title;
    private String description;
    private Double discountRate;
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @OneToMany(mappedBy = "promo")
    private List<PromotionDealer> promotionDealers;

    @OneToMany(mappedBy = "promo")
    private List<PromotionVehicle> promotionVehicles;

    @OneToMany(mappedBy = "promotion")
    private List<OrderDetail> orderDetails;
}

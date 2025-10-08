package com.example.evm.entity.promotion;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.Vehicle;

@Entity
@Table(name = "Promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promo_id")
    private Long promoId;

    // Quan hệ nhiều-1 với Dealer (có thể null nếu là khuyến mãi toàn hệ thống)
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "discount_rate")
    private Double discountRate; // Phần trăm giảm giá (ví dụ: 10.50 cho 10.5%)

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;


    // Quan hệ many-to-many với Vehicle (thông qua bảng PromotionVehicle)
    @ManyToMany
    @JoinTable(
        name = "PromotionVehicle",
        joinColumns = @JoinColumn(name = "promo_id"),
        inverseJoinColumns = @JoinColumn(name = "vehicle_id")
    )
    @JsonIgnore
    private List<Vehicle> vehicles = new ArrayList<>();

    // Quan hệ many-to-many với Dealer (thông qua bảng PromotionDealer)
    @ManyToMany
    @JoinTable(
        name = "PromotionDealer",
        joinColumns = @JoinColumn(name = "promo_id"),
        inverseJoinColumns = @JoinColumn(name = "dealer_id")
    )
    @JsonIgnore
    private List<Dealer> applicableDealers = new ArrayList<>();

    // Helper method - kiểm tra khuyến mãi còn hiệu lực
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        // Kiểm tra xem ngày hôm nay có nằm trong khoảng [startDate, endDate] hay không
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    // Helper method - kiểm tra khuyến mãi áp dụng cho vehicle cụ thể
    public boolean appliesToVehicle(Long vehicleId) {
        return vehicles.stream()
                 .anyMatch(vehicle -> vehicle.getVehicleId().equals(vehicleId));
    }

    // Helper method - kiểm tra khuyến mãi áp dụng cho dealer cụ thể
    public boolean appliesToDealer(Long dealerId) {
        // Nếu là khuyến mãi toàn hệ thống (dealer = null) hoặc áp dụng cho dealer cụ thể
        return dealer == null || 
               applicableDealers.stream()
                        .anyMatch(d -> d.getDealerId().equals(dealerId));
    }

    // Helper method - tính giá sau khuyến mãi
    public Double calculateDiscountedPrice(Double originalPrice) {
        if (discountRate == null) return originalPrice;
        return originalPrice * (1 - discountRate / 100);
    }

    @Override
    public String toString() {
        return "Promotion{" +
                "promoId=" + promoId +
                ", title='" + title + '\'' +
                ", discountRate=" + discountRate +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
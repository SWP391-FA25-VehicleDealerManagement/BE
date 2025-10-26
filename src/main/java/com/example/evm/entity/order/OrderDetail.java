package com.example.evm.entity.order;

import com.example.evm.entity.promotion.Promotion;
import com.example.evm.entity.vehicle.Vehicle;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "OrderDetail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderdetail_id")
    private Long orderDetailId;

    // Quan hệ nhiều-1 với Order
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Quan hệ nhiều-1 với Vehicle
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Quan hệ nhiều-1 với Promotion (có thể null nếu không có khuyến mãi)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private Double price;

    // Helper method để tính tổng tiền cho detail này
    public Double getTotalPrice() {
        return price * quantity;
    }

    // Helper method để apply promotion
    public void applyPromotion(Promotion promotion) {
        if (promotion != null && promotion.getDiscountRate() != null) {
            this.promotion = promotion;
            this.price = this.price * (1 - promotion.getDiscountRate() / 100);
        }
    }

    @Override
    public String toString() {
        
        // 🟢 Lấy tên Variant trực tiếp từ Vehicle (Schema mới)
        String vehicleName = "N/A (Vehicle Info Missing)";
        if (vehicle != null && vehicle.getVariant() != null) {
            vehicleName = vehicle.getVariant().getName();
        }
        
        return "OrderDetail{" +
                "orderDetailId=" + orderDetailId +
                "vehicle=" + vehicleName +
                ", promotion=" + (promotion != null ? promotion.getTitle() : "null") +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }

    // Helper methods để expose ID
    public Long getOrderId() {
        return order != null ? order.getOrderId() : null;
    }

    public Long getVehicleId() {
        return vehicle != null ? vehicle.getVehicleId() : null;
    }

    public Long getPromotionId() {
        return promotion != null ? promotion.getPromoId() : null;
    }
}
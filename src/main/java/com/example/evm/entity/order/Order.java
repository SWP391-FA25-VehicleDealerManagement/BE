package com.example.evm.entity.order;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.evm.entity.customer.Customer;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.user.User;

@Entity
@Table(name = "`Order`")  // Escape từ khóa SQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    // Quan hệ với Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Quan hệ với User (nhân viên tạo đơn)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Quan hệ với Dealer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "createddate")
    private LocalDateTime createdDate;

    @Column(name = "status", length = 50)
    private String status = "PENDING"; // PENDING, CONFIRMED, DELIVERED, CANCELLED

    // Quan hệ 1-nhiều với OrderDetail
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    // Helper methods
    public void addOrderDetail(OrderDetail detail) {
        orderDetails.add(detail);
        detail.setOrder(this);
    }

    public void removeOrderDetail(OrderDetail detail) {
        orderDetails.remove(detail);
        detail.setOrder(null);
    }

    public Double calculateTotalPrice() {
        return orderDetails.stream()
                .mapToDouble(detail -> detail.getPrice() * detail.getQuantity())
                .sum();
    }
}
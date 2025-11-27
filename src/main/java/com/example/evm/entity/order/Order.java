package com.example.evm.entity.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // Quan hệ với Customer (nullable cho đơn nội bộ dealer)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;

    // Quan hệ với User (nhân viên tạo đơn)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Quan hệ với Dealer
    @JsonIgnore
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

    // Trường tạm để tính số tiền đã thanh toán (không lưu vào DB)
    @Transient
    private Double amountPaid = 0.0;

    // Quan hệ 1-nhiều với OrderDetail
    @JsonIgnore
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

    // Helper methods để expose ID
    public Long getCustomerId() {
        return customer != null ? customer.getCustomerId() : null;
    }

    public Long getUserId() {
        return user != null ? user.getUserId() : null;
    }

    public Long getDealerId() {
        return dealer != null ? dealer.getDealerId() : null;
    }

    // Expose orderDetailId để trả về trong API (mỗi order có 1 detail)
    public Long getOrderDetailId() {
        return orderDetails != null && !orderDetails.isEmpty()
            ? orderDetails.get(0).getOrderDetailId()
            : null;
    }

    // Override getter methods để thêm @JsonIgnore  
    @JsonIgnore
    public Customer getCustomer() {
        return customer;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    @JsonIgnore
    public Dealer getDealer() {
        return dealer;
    }

    @JsonIgnore
    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }
    
}
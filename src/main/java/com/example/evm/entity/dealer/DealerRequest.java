package com.example.evm.entity.dealer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.evm.entity.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Table(name = "DealerRequest")
@Data
public class DealerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Người tạo yêu cầu
    private User createdBy;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "required_date")
    private LocalDateTime requiredDate;

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, SHIPPED, DELIVERED

    @Column(name = "priority", length = 10)
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    // ✅ Dùng BigDecimal thay vì Double
    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    // Quan hệ 1-nhiều với chi tiết yêu cầu
    @OneToMany(mappedBy = "dealerRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DealerRequestDetail> requestDetails = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (requestDate == null) {
            requestDate = LocalDateTime.now();
        }
    }

    // Helper methods
    public void addRequestDetail(DealerRequestDetail detail) {
        requestDetails.add(detail);
        detail.setDealerRequest(this);
    }

    public void removeRequestDetail(DealerRequestDetail detail) {
        requestDetails.remove(detail);
        detail.setDealerRequest(null);
    }

    // ✅ Return BigDecimal
    public BigDecimal calculateTotalAmount() {
        return requestDetails.stream()
                .map(DealerRequestDetail::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
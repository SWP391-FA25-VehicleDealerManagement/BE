package com.example.evm.entity.debt;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.evm.entity.customer.Customer;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.user.User;

@Entity
@Table(name = "Debt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "debt_id")
    private Long debtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Nhân viên quản lý nợ
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "amount_due", precision = 18, scale = 2)
    private BigDecimal amountDue;

    @Column(name = "amount_paid", precision = 18, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate = BigDecimal.ZERO; // Lãi suất hàng tháng (%)

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE"; // ACTIVE, PAID, OVERDUE, CANCELLED

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Quan hệ 1-nhiều với lịch trả nợ
    @OneToMany(mappedBy = "debt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DebtSchedule> debtSchedules = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
        if (amountPaid == null) {
            amountPaid = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
        updateStatus();
    }

    // Helper methods
    public void addDebtSchedule(DebtSchedule schedule) {
        debtSchedules.add(schedule);
        schedule.setDebt(this);
    }

    public void removeDebtSchedule(DebtSchedule schedule) {
        debtSchedules.remove(schedule);
        schedule.setDebt(null);
    }

    public BigDecimal getRemainingAmount() {
        if (amountDue == null) return BigDecimal.ZERO;
        if (amountPaid == null) return amountDue;
        return amountDue.subtract(amountPaid);
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) 
            && getRemainingAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    private void updateStatus() {
        if (getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            status = "PAID";
        } else if (isOverdue()) {
            status = "OVERDUE";
        } else {
            status = "ACTIVE";
        }
    }

    public BigDecimal getTotalInterest() {
        return debtSchedules.stream()
                .map(DebtSchedule::getInterest)
                .filter(interest -> interest != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

package com.example.evm.entity.debt;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "DebtSchedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebtSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debt_id", nullable = false)
    private Debt debt;

    @Column(name = "period_no")
    private Long periodNo;

    @Column(name = "start_balance", precision = 18, scale = 2)
    private BigDecimal startBalance;

    @Column(name = "principal", precision = 18, scale = 2)
    private BigDecimal principal;

    @Column(name = "interest", precision = 18, scale = 2)
    private BigDecimal interest;

    @Column(name = "installment", precision = 18, scale = 2)
    private BigDecimal installment;

    @Column(name = "end_balance", precision = 18, scale = 2)
    private BigDecimal endBalance;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_amount", precision = 18, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, PAID, OVERDUE, PARTIAL

    @Column(name = "notes", length = 255)
    private String notes;

    // Helper methods
    public BigDecimal getRemainingAmount() {
        if (installment == null) return BigDecimal.ZERO;
        BigDecimal paid = (paidAmount != null) ? paidAmount : BigDecimal.ZERO;
        return installment.subtract(paid);
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDate.now().isAfter(dueDate) 
            && getRemainingAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    public void updateStatus() {
        if (paidAmount != null && installment != null && paidAmount.compareTo(installment) >= 0) {
            status = "PAID";
        } else if (paidAmount != null && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            status = "PARTIAL";
        } else if (isOverdue()) {
            status = "OVERDUE";
        } else {
            status = "PENDING";
        }
    }

    // Helper method để expose debtId
    public Long getDebtId() {
        return debt != null ? debt.getDebtId() : null;
    }
}
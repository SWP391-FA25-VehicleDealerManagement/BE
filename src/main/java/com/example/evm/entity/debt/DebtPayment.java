package com.example.evm.entity.debt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.PrePersist;
@Entity
@Table(name = "DebtPayment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebtPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debt_id", nullable = false)
    private Debt debt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private DebtSchedule debtSchedule;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // CASH, BANK_TRANSFER, CREDIT_CARD

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING (chờ EVM xác nhận), CONFIRMED (đã xác nhận), REJECTED (từ chối)

    @Column(name = "confirmed_by", length = 100)
    private String confirmedBy; // Người xác nhận (EVM Staff)

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate; // Ngày giờ xác nhận

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason; // Lý do từ chối (nếu REJECTED)

    @PrePersist
    protected void onCreate() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING"; // Mặc định chờ xác nhận
        }
    }

    // Helper methods để expose ID
    public Long getDebtId() {
        return debt != null ? debt.getDebtId() : null;
    }

    public Long getScheduleId() {
        return debtSchedule != null ? debtSchedule.getScheduleId() : null;
    }
}
package com.example.evm.dto.debt;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho việc tạo thanh toán nợ (DebtPayment)
 * 
 * Không cần order_id vì thanh toán nợ độc lập với đơn hàng
 * Chỉ cần: debt_id (từ URL), amount, payment_method, schedule_id (optional), notes, created_by
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDebtPaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CASH, BANK_TRANSFER, CREDIT_CARD

    private Long scheduleId; // Optional: Nếu thanh toán cho 1 kỳ cụ thể (installment payment)

    private String referenceNumber; // Optional: Số tham chiếu (mã chuyển khoản...)

    private String notes; // Optional: Ghi chú

    private String createdBy; // Người tạo thanh toán (user name hoặc ID)
}

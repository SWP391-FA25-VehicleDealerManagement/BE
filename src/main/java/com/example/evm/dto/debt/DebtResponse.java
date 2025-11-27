package com.example.evm.dto.debt;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebtResponse {
    
    private Long debtId;
    private String debtType; // DEALER_DEBT, CUSTOMER_DEBT
    
    // Thông tin User đầy đủ
    private UserInfo user;
    
    // Thông tin Dealer đầy đủ
    private DealerInfo dealer;
    
    // Thông tin Customer đầy đủ
    private CustomerInfo customer;
    
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private BigDecimal remainingAmount;
    private BigDecimal interestRate;
    
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private String status;
    private String paymentMethod;
    private String notes;
    
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    
    private boolean overdue;
    private BigDecimal totalInterest;
    
    // Nested classes for related entities
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long userId;
        private String username;
        private String fullName;
        private String email;
        private String phoneNumber;
        private String role;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DealerInfo {
        private Long dealerId;
        private String dealerName;
        private String dealerCode;
        private String contactPerson;
        private String email;
        private String phoneNumber;
        private String address;
        private String city;
        private String status;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerInfo {
        private Long customerId;
        private String customerName;
        private String email;
        private String phoneNumber;
        private String address;
        private String city;
        private String identityCard;
        private String customerType;
    }
}


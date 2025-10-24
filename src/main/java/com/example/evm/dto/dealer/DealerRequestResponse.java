package com.example.evm.dto.dealer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealerRequestResponse {
    private Long requestId;
    
    // Dealer & User info
    private Long dealerId;
    private String dealerName;
    private Long userId;
    private String userFullName;
    private String userRole;
    
    // Request info
    private LocalDateTime requestDate;
    private LocalDateTime requiredDate;
    private String status;
    private String priority;
    private String notes;
    private BigDecimal totalAmount;
    
    // Workflow tracking
    private LocalDateTime approvedDate;
    private String approvedBy;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveryDate;
    
    // Details
    private List<RequestDetailResponse> requestDetails;
}


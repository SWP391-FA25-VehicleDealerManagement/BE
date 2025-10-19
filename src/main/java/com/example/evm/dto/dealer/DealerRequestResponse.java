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
    
    // Chỉ thông tin cần thiết
    private String dealerName;
    private String userFullName;
    private String userRole;
    
    private LocalDateTime requestDate;
    private LocalDateTime requiredDate;
    private String status;
    private String priority;
    private String notes;
    private BigDecimal totalAmount;
    
    private List<RequestDetailResponse> requestDetails;
}


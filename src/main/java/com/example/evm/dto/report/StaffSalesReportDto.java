package com.example.evm.dto.report;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffSalesReportDto {
    private Long userId;
    private String userName;
    private String fullName;
    private String phone;
    private String email;
    private String role;
    private String dealerName;
    private Integer year;
    private Integer month;
    private Long orderId;
    private Double totalPrice;
    private LocalDateTime createdDate;
}

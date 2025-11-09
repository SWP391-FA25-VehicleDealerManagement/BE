package com.example.evm.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesByStaffDto {
    private Long userId;
    private String userName;
    private String fullName;
    private String phone;
    private String email;
    private String role; 
    private String dealerName;
    private Integer year;
    private Integer month;
    private Long totalOrders;
    private Double totalRevenue;
}

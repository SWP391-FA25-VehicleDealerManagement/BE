package com.example.evm.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DealerSalesReportDto {
    private Long dealerId;
    private String dealerName;
    private String phone;
    private String address;
    private Long totalOrders;
    private Double totalRevenue;
}

package com.example.evm.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DealerInventoryReportDto {
    private Long dealerId;
    private String dealerName;
    private String phone;
    private String address;
    private Long totalVehicles;
    private Long availableVehicles;
    private Long soldVehicles;
}

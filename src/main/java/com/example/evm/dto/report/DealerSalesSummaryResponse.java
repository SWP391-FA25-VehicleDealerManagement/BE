package com.example.evm.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Báo cáo tổng hợp doanh thu theo đại lý")
public class DealerSalesSummaryResponse {

    private Long dealerId;
    private String dealerName;
    private String phone;
    private String address;
    private Integer year;
    private Integer month; 
    private Double totalRevenue;
    private Long totalOrders;
}

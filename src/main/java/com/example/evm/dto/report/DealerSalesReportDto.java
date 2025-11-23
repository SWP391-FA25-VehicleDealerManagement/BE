package com.example.evm.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Báo cáo doanh thu và số lượng đơn hàng của 1 đại lý")
public class DealerSalesReportDto {

    private Long dealerId;
    private String dealerName;
    private Integer year;
    private Integer month;
    private Double totalRevenue;
    private Long totalOrders;
}

package com.example.evm.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = " Báo cáo tồn kho của hãng sản xuất")
public class ManufacturerInventoryReportDto {

    private Long manufacturerId;
    private String manufacturerName;
    private String location;
    private Long totalVehicles;
    private Long inStock;
    private Long soldVehicles;
}

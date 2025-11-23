package com.example.evm.service.report;

import com.example.evm.dto.report.DealerInventoryReportDto;
import com.example.evm.dto.report.DealerSalesReportDto;
import com.example.evm.dto.report.DealerSalesSummaryResponse;
import com.example.evm.dto.report.DealerTurnoverReportDto;
import com.example.evm.dto.report.ManufacturerInventoryReportDto;
import com.example.evm.dto.report.SalesByStaffDto;

import java.util.List;
import java.util.Map;

public interface ReportService {

    List<SalesByStaffDto> getSalesByStaff(Long dealerId, Integer year, Integer month);
    Map<String, Object> getStaffSalesReport(Long userId, Integer year, Integer month);
    List<DealerInventoryReportDto> getInventoryReport();
    List<ManufacturerInventoryReportDto> getManufacturerInventoryReport();
    List<DealerTurnoverReportDto> getTurnoverReport();
    List<DealerSalesReportDto> getDealerSalesReport(Long dealerId, Integer year, Integer month);
    List<DealerSalesSummaryResponse> getAllDealersSalesSummary(Integer year,Integer month);
}

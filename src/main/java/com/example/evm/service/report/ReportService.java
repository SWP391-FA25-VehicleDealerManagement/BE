package com.example.evm.service.report;

import com.example.evm.dto.report.*;

import java.util.List;

public interface ReportService {
    List<DealerSalesReportDto> getDealerSalesReport();
    List<SalesByStaffDto> getSalesByStaff(Long dealerId);
    List<DealerInventoryReportDto> getInventoryReport();
    List<DealerTurnoverReportDto> getTurnoverReport();
}

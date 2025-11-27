package com.example.evm.controller.report;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.report.DealerInventoryReportDto;
import com.example.evm.dto.report.DealerSalesReportDto;
import com.example.evm.dto.report.DealerSalesSummaryResponse;
import com.example.evm.dto.report.DealerTurnoverReportDto;
import com.example.evm.dto.report.ManufacturerInventoryReportDto;
import com.example.evm.dto.report.SalesByStaffDto;
import com.example.evm.service.report.ReportService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    // --- Báo cáo doanh thu của 1 đại lý
    @GetMapping("/dealer-sales")
    @PreAuthorize("hasAnyAuthority('DEALER_MANAGER')")
    @Operation(summary = "Báo cáo doanh thu của 1 đại lý")
    public ResponseEntity<ApiResponse<List<DealerSalesReportDto>>> getDealerSalesReport(
            @RequestParam Long dealerId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        List<DealerSalesReportDto> report = reportService.getDealerSalesReport(dealerId, year, month);
        return ResponseEntity.ok(new ApiResponse<>(true, "Báo cáo doanh thu của đại lý", report));
    }

    // --- Báo cáo doanh số theo nhân viên của 1 đại lý
    @GetMapping("/staff-sales/{dealerId}")
    @PreAuthorize("hasAnyAuthority('DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<SalesByStaffDto>>> getSalesByStaff(
        @PathVariable Long dealerId,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month) {
        List<SalesByStaffDto> reportData = reportService.getSalesByStaff(dealerId, year, month);
        return ResponseEntity.ok(new ApiResponse<>(true, "Báo cáo doanh số nhân viên lấy thành công", reportData));
    }

    // --- Báo cáo doanh số của 1 nhân viên
    @GetMapping("/staff-sales-report")
    @Operation(summary = " Báo cáo doanh thu của 1 nhân viên cụ thể")
    @PreAuthorize("hasAnyAuthority('DEALER_MANAGER', 'DEALER_STAFF')")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getStaffSalesReport(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        Map<String, Object> report = reportService.getStaffSalesReport(userId, year, month);
        return ResponseEntity.ok(new ApiResponse<>(true, "Báo cáo doanh thu của nhân viên", report));
    }



    // --- Báo cáo doanh thu của tất cả đại lý
    @GetMapping("/dealers/summary")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    @Operation(summary = " Báo cáo tổng hợp doanh thu của tất cả đại lý (chỉ hãng xem)")
    public ResponseEntity<ApiResponse<List<DealerSalesSummaryResponse>>> getAllDealersSalesSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        List<DealerSalesSummaryResponse> report = reportService.getAllDealersSalesSummary(year, month);
        return ResponseEntity.ok(new ApiResponse<>(true, "Báo cáo tổng hợp doanh thu đại lý", report));
    }


    // --- Báo cáo tồn kho
    @GetMapping("/inventory")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<DealerInventoryReportDto>>> getInventoryReport() { 
        List<DealerInventoryReportDto> reportData = reportService.getInventoryReport();
        return ResponseEntity.ok(new ApiResponse<>(true, "Báo cáo tồn kho lấy thành công", reportData));
    }

    //  Báo cáo tồn kho hãng sản xuất
    @GetMapping("/inventory/manufacturer")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<ManufacturerInventoryReportDto>>> getManufacturerInventoryReport() {
        List<ManufacturerInventoryReportDto> report = reportService.getManufacturerInventoryReport();
        return ResponseEntity.ok(new ApiResponse<>(true, "Báo cáo tồn kho của hãng sản xuất", report));
    }


    // --- Báo cáo tốc độ tiêu thụ
    @GetMapping("/turnover")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<DealerTurnoverReportDto>>> getTurnoverReport() { 
        List<DealerTurnoverReportDto> reportData = reportService.getTurnoverReport();
        return ResponseEntity.ok(new ApiResponse<>(true, "Báo cáo tốc độ tiêu thụ lấy thành công", reportData));
    }
}

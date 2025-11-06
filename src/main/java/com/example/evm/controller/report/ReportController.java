package com.example.evm.controller.report;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    // --- Báo cáo doanh số các đại lý
    @GetMapping("/dealer-sales")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Object>> getDealerSalesReport() {
        Object reportData = reportService.getDealerSalesReport();
        return ResponseEntity.ok(new ApiResponse<>(true, "Báo cáo doanh số đại lý lấy thành công", reportData));
    }

    // --- Báo cáo doanh số theo nhân viên của 1 đại lý
    @GetMapping("/staff-sales/{dealerId}")
    @PreAuthorize("hasAnyAuthority('DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<Object>> getSalesByStaff(@PathVariable Long dealerId) {
        Object reportData = reportService.getSalesByStaff(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Báo cáo doanh số nhân viên lấy thành công", reportData));
    }

    // --- Báo cáo tồn kho
    @GetMapping("/inventory")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Object>> getInventoryReport() { 
        Object reportData = reportService.getInventoryReport();
        return ResponseEntity.ok(new ApiResponse<>(true, "Báo cáo tồn kho lấy thành công", reportData));
    }

    // --- Báo cáo tốc độ tiêu thụ
    @GetMapping("/turnover")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Object>> getTurnoverReport() { 
        Object reportData = reportService.getTurnoverReport();
        return ResponseEntity.ok(new ApiResponse<>(true, "Báo cáo tốc độ tiêu thụ lấy thành công", reportData));
    }
}

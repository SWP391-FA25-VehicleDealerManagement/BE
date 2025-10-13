package com.example.evm.controller.report;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/revenue/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMonthlyRevenue() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Monthly revenue fetched", reportService.getMonthlyRevenue()));
    }

    @GetMapping("/dealer/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDealerPerformance() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer performance fetched", reportService.getDealerPerformance()));
    }

    @GetMapping("/vehicle/top-selling")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopSellingVehicles() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Top selling vehicles fetched", reportService.getTopSellingVehicles()));
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInventoryStatus() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory status fetched", reportService.getInventoryStatus()));
    }

    @GetMapping("/debt")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDebtSummary() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt summary fetched", reportService.getDebtSummary()));
    }
}

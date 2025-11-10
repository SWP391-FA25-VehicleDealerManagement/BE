package com.example.evm.service.report;

import com.example.evm.dto.report.DealerInventoryReportDto;
import com.example.evm.dto.report.DealerSalesReportDto;
import com.example.evm.dto.report.DealerSalesSummaryResponse;
import com.example.evm.dto.report.DealerTurnoverReportDto;
import com.example.evm.dto.report.SalesByStaffDto;
import com.example.evm.dto.report.StaffSalesReportDto;
import com.example.evm.repository.order.OrderRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public List<DealerSalesReportDto> getDealerSalesReport(Long dealerId, Integer year, Integer month) {
        return orderRepository.getDealerSalesReport(dealerId, year, month);
    }

    @Override
    public List<DealerSalesSummaryResponse> getAllDealersSalesSummary(Integer year, Integer month) {
        return orderRepository.getAllDealersSalesSummary(year, month);
    }

    @Override
    public List<SalesByStaffDto> getSalesByStaff(Long dealerId, Integer year, Integer month) {
        log.info("📈 Generating staff sales report for dealer {}", dealerId);
        return orderRepository.getSalesByStaff(dealerId, year, month);
    }

    @Override
    public List<DealerInventoryReportDto> getInventoryReport() {
        log.info("📦 Generating dealer inventory report...");
        return vehicleRepository.getDealerInventoryReport();
    }

    @Override
    public List<DealerTurnoverReportDto> getTurnoverReport() {
        log.info("📉 Generating dealer turnover rate report...");
        return orderRepository.getDealerTurnoverReport();
    }

    @Override
    public Map<String, Object> getStaffSalesReport(Long userId, Integer year, Integer month) {
        log.info("📊 Generating sales report for staff ID {}", userId);

        List<StaffSalesReportDto> results = orderRepository.getStaffSalesReport(userId, year, month);

        if (results == null || results.isEmpty()) {
            throw new RuntimeException("Không tìm thấy dữ liệu báo cáo cho nhân viên này");
        }

        StaffSalesReportDto dto = results.get(0);

        // Trả response map
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("userId", dto.getUserId());
        report.put("userName", dto.getUserName());
        report.put("fullName", dto.getFullName());
        report.put("phone", dto.getPhone());
        report.put("email", dto.getEmail());
        report.put("role", dto.getRole());
        report.put("dealerName", dto.getDealerName());
        report.put("year", year);
        report.put("month", month);

        report.put("orders", results.stream()
            .map(r -> Map.of(
            "orderId", r.getOrderId(),
            "totalPrice", r.getTotalPrice(),
            "createdDate", r.getCreatedDate()
        ))
        .toList());
        return report;
    }
}

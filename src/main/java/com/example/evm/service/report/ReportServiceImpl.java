package com.example.evm.service.report;

import com.example.evm.dto.report.*;
import com.example.evm.repository.order.OrderRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public List<DealerSalesReportDto> getDealerSalesReport() {
        log.info("📊 Generating dealer sales report...");
        return orderRepository.getDealerSalesReport();
    }

    @Override
    public List<SalesByStaffDto> getSalesByStaff(Long dealerId) {
        log.info("📈 Generating staff sales report for dealer {}", dealerId);
        return orderRepository.getSalesByStaff(dealerId);
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
}

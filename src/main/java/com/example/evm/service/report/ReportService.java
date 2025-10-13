package com.example.evm.service.report;

import com.example.evm.repository.report.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public List<Map<String, Object>> getMonthlyRevenue() {
        return reportRepository.getMonthlyRevenue();
    }

    public List<Map<String, Object>> getDealerPerformance() {
        return reportRepository.getDealerPerformance();
    }

    public List<Map<String, Object>> getTopSellingVehicles() {
        return reportRepository.getTopSellingVehicles();
    }

    public List<Map<String, Object>> getInventoryStatus() {
        return reportRepository.getInventoryStatus();
    }

    public List<Map<String, Object>> getDebtSummary() {
        return reportRepository.getDebtSummary();
    }
}

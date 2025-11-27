package com.example.evm.service.testDrive;

import com.example.evm.dto.testDrive.TestDriveWithDetailsDTO;
import com.example.evm.entity.testDrive.TestDrive;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.testDrive.TestDriveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestDriveService {

    private final TestDriveRepository testDriveRepository;

    // ==================== READ OPERATIONS ====================
    
    public List<TestDrive> getAllTestDrives() {
        return testDriveRepository.findAllWithDetails();
    }

    public TestDrive getTestDriveById(Long id) {
        List<TestDrive> results = testDriveRepository.findByIdWithDetails(id);
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Test drive not found with id: " + id);
        }
        return results.get(0);
    }

    public List<TestDrive> getTestDrivesByDealer(Long dealerId) {
        return testDriveRepository.findByDealerWithDetails(dealerId);
    }

    public List<TestDrive> getTestDrivesByCustomer(Long customerId) {
        return testDriveRepository.findByCustomerWithDetails(customerId);
    }

    public List<TestDrive> getTestDrivesByVehicle(Long vehicleId) {
        return testDriveRepository.findByVehicleVehicleId(vehicleId);
    }

    public List<TestDrive> getTestDrivesByStatus(String status) {
        return testDriveRepository.findByStatusWithDetails(status);
    }

    // Sử dụng query tổng hợp với dealerId
    public List<TestDrive> getUpcomingTestDrives(Long dealerId) {
        return testDriveRepository.findUpcomingTestDrives(dealerId, LocalDateTime.now());
    }

    // Sử dụng query tổng hợp với dealerId = null
    public List<TestDrive> getAllUpcomingTestDrives() {
        return testDriveRepository.findUpcomingTestDrives(null, LocalDateTime.now());
    }

    // Sử dụng query tổng hợp cho date range
    public List<TestDrive> getTestDrivesByDateRange(Long dealerId, LocalDateTime startDate, LocalDateTime endDate) {
        return testDriveRepository.findByDateRange(dealerId, startDate, endDate);
    }

    public List<TestDrive> getOverdueTestDrives(LocalDateTime currentDate) {
        return testDriveRepository.findOverdueTestDrives(currentDate);
    }

    // ==================== CREATE OPERATIONS ====================
    
    @Transactional
    public TestDrive scheduleTestDrive(TestDrive testDrive) {
        // Để DB tự động tạo ID
        testDrive.setTestDriveId(null);
        testDrive.setCreatedDate(LocalDateTime.now());
        testDrive.setStatus("SCHEDULED");
        
        TestDrive savedTestDrive = testDriveRepository.save(testDrive);
        log.info("Created test drive ID: {} scheduled at: {}", 
                savedTestDrive.getTestDriveId(), savedTestDrive.getScheduledDate());
        
        return savedTestDrive;
    }

    // ==================== UPDATE OPERATIONS ====================
    
    @Transactional
    public TestDrive updateTestDrive(Long id, TestDrive testDriveDetails) {
        TestDrive testDrive = getTestDriveById(id);
        
        if (testDriveDetails.getScheduledDate() != null) {
            testDrive.setScheduledDate(testDriveDetails.getScheduledDate());
        }
        if (testDriveDetails.getStatus() != null) {
            testDrive.setStatus(testDriveDetails.getStatus());
        }
        if (testDriveDetails.getNotes() != null) {
            testDrive.setNotes(testDriveDetails.getNotes());
        }
        if (testDriveDetails.getAssignedBy() != null) {
            testDrive.setAssignedBy(testDriveDetails.getAssignedBy());
        }
        if (testDriveDetails.getCustomer() != null) {
            testDrive.setCustomer(testDriveDetails.getCustomer());
        }
        if (testDriveDetails.getVehicle() != null) {
            testDrive.setVehicle(testDriveDetails.getVehicle());
        }
        if (testDriveDetails.getDealer() != null) {
            testDrive.setDealer(testDriveDetails.getDealer());
        }
        
        return testDriveRepository.save(testDrive);
    }

    @Transactional
    public TestDrive updateTestDriveStatus(Long id, String status, String notes) {
        TestDrive testDrive = getTestDriveById(id);
        testDrive.setStatus(status);
        
        if (notes != null) {
            testDrive.setNotes(notes);
        }
        
        return testDriveRepository.save(testDrive);
    }

    @Transactional
    public TestDrive rescheduleTestDrive(Long id, LocalDateTime newDate) {
        TestDrive testDrive = getTestDriveById(id);
        testDrive.setScheduledDate(newDate);
        testDrive.setStatus("RESCHEDULED");
        
        return testDriveRepository.save(testDrive);
    }

    // ==================== DELETE OPERATIONS ====================
    
    @Transactional
    public void deleteTestDrive(Long id) {
        TestDrive testDrive = getTestDriveById(id);
        testDriveRepository.delete(testDrive);
        log.info("Test drive deleted with id: {}", id);
    }

    @Transactional
    public TestDrive cancelTestDrive(Long id) {
        TestDrive testDrive = getTestDriveById(id);
        testDrive.setStatus("CANCELLED");
        TestDrive savedTestDrive = testDriveRepository.save(testDrive);
        log.info("Test drive cancelled with id: {}", id);
        return savedTestDrive;
    }

    // ==================== STATISTICS & REPORTING ====================
    // Thực hiện tính toán trong Java thay vì SQL để tránh query phức tạp
    
    public Map<String, Object> getTestDriveStats(Long dealerId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Lấy tất cả test drives của dealer
        List<TestDrive> allTestDrives = getTestDrivesByDealer(dealerId);
        stats.put("total", allTestDrives.size());
        
        // Đếm theo status bằng Java Stream thay vì SQL GROUP BY
        Map<String, Long> statusCounts = allTestDrives.stream()
                .collect(Collectors.groupingBy(
                    TestDrive::getStatus, 
                    Collectors.counting()));
        
        stats.put("scheduled", statusCounts.getOrDefault("SCHEDULED", 0L));
        stats.put("completed", statusCounts.getOrDefault("COMPLETED", 0L));
        stats.put("cancelled", statusCounts.getOrDefault("CANCELLED", 0L));
        stats.put("rescheduled", statusCounts.getOrDefault("RESCHEDULED", 0L));
        
        // Đếm upcoming bằng cách filter kết quả đã có
        long upcoming = allTestDrives.stream()
                .filter(td -> "SCHEDULED".equals(td.getStatus()) 
                           && td.getScheduledDate().isAfter(LocalDateTime.now()))
                .count();
        stats.put("upcoming", upcoming);
        
        return stats;
    }

    public Map<String, Object> getMonthlyTestDriveStats(Long dealerId) {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.from(now);
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        // Sử dụng query tổng hợp
        List<TestDrive> monthlyTestDrives = getTestDrivesByDateRange(dealerId, startOfMonth, endOfMonth);
        
        stats.put("month", currentMonth.getMonth().toString());
        stats.put("year", currentMonth.getYear());
        stats.put("total", monthlyTestDrives.size());
        
        // Đếm theo status bằng Java Stream
        Map<String, Long> statusCounts = monthlyTestDrives.stream()
                .collect(Collectors.groupingBy(
                    TestDrive::getStatus, 
                    Collectors.counting()));
        
        stats.put("scheduled", statusCounts.getOrDefault("SCHEDULED", 0L));
        stats.put("completed", statusCounts.getOrDefault("COMPLETED", 0L));
        stats.put("cancelled", statusCounts.getOrDefault("CANCELLED", 0L));
        stats.put("rescheduled", statusCounts.getOrDefault("RESCHEDULED", 0L));
        
        return stats;
    }

}


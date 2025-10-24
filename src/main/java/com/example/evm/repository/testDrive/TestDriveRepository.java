package com.example.evm.repository.testDrive;

import com.example.evm.entity.testDrive.TestDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestDriveRepository extends JpaRepository<TestDrive, Long> {
    
    // ==================== SPRING DATA JPA DERIVED QUERIES ====================
    
    
    List<TestDrive> findByDealerDealerId(Long dealerId);
    List<TestDrive> findByCustomerCustomerId(Long customerId);
    List<TestDrive> findByVehicleVehicleId(Long vehicleId); 
    List<TestDrive> findByStatus(String status);
    List<TestDrive> findByDealerDealerIdAndStatus(Long dealerId, String status);
    List<TestDrive> findByCustomerCustomerIdAndStatus(Long customerId, String status);
    List<TestDrive> findByAssignedBy(String assignedBy);
    List<TestDrive> findByDealerDealerIdAndAssignedBy(Long dealerId, String assignedBy);
    
    // Thay thế cho findTestDrivesWithNotes - sử dụng Spring Data JPA
    List<TestDrive> findByDealerDealerIdAndNotesIsNotNull(Long dealerId);
    
    // ==================== CUSTOM QUERIES - CHỈ GIỮ LẠI CÁC QUERY THỰC SỰ CẦN THIẾT ====================
    
    // Query tổng hợp cho upcoming test drives (bao gồm cả dealer cụ thể và toàn hệ thống)
    @Query("SELECT t FROM TestDrive t WHERE " +
           "(:dealerId IS NULL OR t.dealer.dealerId = :dealerId) AND " +
           "t.scheduledDate >= :currentDate AND t.status = 'SCHEDULED' " +
           "ORDER BY t.scheduledDate ASC")
    List<TestDrive> findUpcomingTestDrives(@Param("dealerId") Long dealerId, 
                                          @Param("currentDate") LocalDateTime currentDate);
    
    // Query tổng hợp cho date range (có thể filter theo dealer hoặc không)
    @Query("SELECT t FROM TestDrive t WHERE " +
           "(:dealerId IS NULL OR t.dealer.dealerId = :dealerId) AND " +
           "t.scheduledDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.scheduledDate ASC")
    List<TestDrive> findByDateRange(@Param("dealerId") Long dealerId,
                                   @Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    // Overdue test drives - cần thiết vì logic phức tạp
    @Query("SELECT t FROM TestDrive t WHERE t.scheduledDate < :currentDate AND t.status = 'SCHEDULED'")
    List<TestDrive> findOverdueTestDrives(@Param("currentDate") LocalDateTime currentDate);
}
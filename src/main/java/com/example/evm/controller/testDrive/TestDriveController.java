package com.example.evm.controller.testDrive;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.testDrive.CreateTestDriveRequest;
import com.example.evm.dto.testDrive.TestDriveWithDetailsDTO;
import com.example.evm.entity.customer.Customer;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.testDrive.TestDrive;
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.service.testDrive.TestDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/testdrives") // Base URL cho tất cả API test drive
@RequiredArgsConstructor
public class TestDriveController {

    private final TestDriveService testDriveService;

    // ==================== CREATE ====================
    /**
     * API tạo lịch thử xe mới
     * Method: POST
     * URL: /api/testdrives/create-test-drive
     * Roles: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     */
    @PostMapping("create-test-drive")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<TestDrive>> scheduleTestDrive(
            @Valid @RequestBody CreateTestDriveRequest request) {
        try {
            // Convert DTO to Entity
            TestDrive testDrive = new TestDrive();
            
            // Set relationships (using proxy to avoid unnecessary queries)
            Dealer dealer = new Dealer();
            dealer.setDealerId(request.getDealerId());
            testDrive.setDealer(dealer);
            
            Customer customer = new Customer();
            customer.setCustomerId(request.getCustomerId());
            testDrive.setCustomer(customer);
            
            Vehicle vehicle = new Vehicle();
            vehicle.setVehicleId(request.getVehicleId());
            testDrive.setVehicle(vehicle);
            
            // Set other fields
            testDrive.setScheduledDate(request.getScheduledDate());
            testDrive.setNotes(request.getNotes());
            testDrive.setAssignedBy(request.getAssignedBy());
            
            // Gọi service để lưu lịch thử xe
            TestDrive scheduledTestDrive = testDriveService.scheduleTestDrive(testDrive);
            log.info("Test drive scheduled ID: {} for customer {} with vehicle {}",
                    scheduledTestDrive.getTestDriveId(),
                    request.getCustomerId(),
                    request.getVehicleId());

            return ResponseEntity.ok(new ApiResponse<>(true,
                    "Test drive scheduled successfully",
                    scheduledTestDrive));
        } catch (Exception e) {
            log.error("Error scheduling test drive", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false,
                    "Failed to schedule test drive: " + e.getMessage(),
                    null));
        }
    }

    // ==================== READ ====================

    /** Lấy tất cả lịch thử xe */
    @GetMapping("get-all-test-drives")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<TestDriveWithDetailsDTO>>> getAllTestDrives() {
        try {
            List<TestDrive> testDrives = testDriveService.getAllTestDrives();
            List<TestDriveWithDetailsDTO> dtos = testDrives.stream()
                    .map(TestDriveWithDetailsDTO::fromTestDrive)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "All test drives retrieved", dtos));
        } catch (Exception e) {
            log.error("Error retrieving all test drives", e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /** Lấy lịch thử xe theo ID */
    @GetMapping("get-test-drive-by-id/{id}")
    public ResponseEntity<ApiResponse<TestDriveWithDetailsDTO>> getTestDriveById(@PathVariable Long id) {
        try {
            TestDrive testDrive = testDriveService.getTestDriveById(id);
            TestDriveWithDetailsDTO dto = TestDriveWithDetailsDTO.fromTestDrive(testDrive);
            return ResponseEntity.ok(new ApiResponse<>(true, "Retrieved test drive", dto));
        } catch (Exception e) {
            log.error("Error retrieving test drive {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    /** Lấy lịch thử xe theo dealer ID */
    @GetMapping("get-test-drives-by-dealer/{dealerId}")
    public ResponseEntity<ApiResponse<List<TestDriveWithDetailsDTO>>> getTestDrivesByDealer(@PathVariable Long dealerId) {
        try {
            List<TestDrive> testDrives = testDriveService.getTestDrivesByDealer(dealerId);
            List<TestDriveWithDetailsDTO> dtos = testDrives.stream()
                    .map(TestDriveWithDetailsDTO::fromTestDrive)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "Test drives for dealer retrieved", dtos));
        } catch (Exception e) {
            log.error("Error retrieving test drives for dealer {}", dealerId, e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /** Lấy lịch thử xe theo customer ID */
    @GetMapping("get-test-drives-by-customer/{customerId}")
    public ResponseEntity<ApiResponse<List<TestDriveWithDetailsDTO>>> getTestDrivesByCustomer(@PathVariable Long customerId) {
        try {
            List<TestDrive> testDrives = testDriveService.getTestDrivesByCustomer(customerId);
            List<TestDriveWithDetailsDTO> dtos = testDrives.stream()
                    .map(TestDriveWithDetailsDTO::fromTestDrive)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "Customer test drives retrieved", dtos));
        } catch (Exception e) {
            log.error("Error retrieving test drives for customer {}", customerId, e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /** Lấy lịch thử xe theo trạng thái (status) */
    @GetMapping("get-test-drives-by-status/{status}")
    public ResponseEntity<ApiResponse<List<TestDriveWithDetailsDTO>>> getTestDrivesByStatus(@PathVariable String status) {
        try {
            List<TestDrive> testDrives = testDriveService.getTestDrivesByStatus(status);
            List<TestDriveWithDetailsDTO> dtos = testDrives.stream()
                    .map(TestDriveWithDetailsDTO::fromTestDrive)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "Test drives by status retrieved", dtos));
        } catch (Exception e) {
            log.error("Error retrieving test drives with status {}", status, e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // ==================== UPDATE ====================

    /** Cập nhật trạng thái (status) của lịch thử xe */
    @PutMapping("update-test-drive-status/{id}/status")
    public ResponseEntity<ApiResponse<TestDrive>> updateTestDriveStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {
        try {
            TestDrive updated = testDriveService.updateTestDriveStatus(id, status, notes);
            return ResponseEntity.ok(new ApiResponse<>(true, "Status updated", updated));
        } catch (Exception e) {
            log.error("Error updating status {}", id, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /** Đổi lịch thử xe sang ngày khác */
    @PutMapping("reschedule-test-drive/{id}/reschedule")
    public ResponseEntity<ApiResponse<TestDrive>> rescheduleTestDrive(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDate) {
        try {
            TestDrive updated = testDriveService.rescheduleTestDrive(id, newDate);
            return ResponseEntity.ok(new ApiResponse<>(true, "Rescheduled successfully", updated));
        } catch (Exception e) {
            log.error("Error rescheduling test drive {}", id, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // ==================== DELETE ====================

    /** Xóa lịch thử xe */
    @DeleteMapping("delete-test-drive/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteTestDrive(@PathVariable Long id) {
        try {
            testDriveService.deleteTestDrive(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting test drive {}", id, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /** Hủy lịch thử xe (status = CANCELLED) */
    @DeleteMapping("cancel-test-drive/{id}/cancel")
    public ResponseEntity<ApiResponse<TestDrive>> cancelTestDrive(@PathVariable Long id) {
        try {
            TestDrive cancelled = testDriveService.cancelTestDrive(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Cancelled successfully", cancelled));
        } catch (Exception e) {
            log.error("Error cancelling test drive {}", id, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }


}
    
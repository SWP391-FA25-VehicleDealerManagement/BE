package com.example.evm.controller.debt;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.entity.debt.Debt;
import com.example.evm.entity.debt.DebtPayment;
import com.example.evm.entity.debt.DebtSchedule;
import com.example.evm.service.debt.DebtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;

    // ==========================================================
    // =============== CREATE OPERATIONS ========================
    // ==========================================================

    /**
     * API tạo mới một khoản nợ (Debt)
     * URL: POST /api/debts
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     * Dữ liệu gửi lên: Debt (JSON)
     * Kết quả: Debt vừa được tạo hoặc thông báo lỗi
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Debt>> createDebt(@Valid @RequestBody Debt debt) {
        try {
            Debt createdDebt = debtService.createDebt(debt);
            return ResponseEntity.ok(new ApiResponse<>(true, "Debt created successfully", createdDebt));
        } catch (Exception e) {
            log.error("Error creating debt", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to create debt: " + e.getMessage(), null));
        }
    }

    /**
     * API ghi nhận một khoản thanh toán (DebtPayment) cho một khoản nợ
     * URL: POST /api/debts/{debtId}/payments
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     * Dữ liệu gửi lên: DebtPayment (JSON)
     * Kết quả: DebtPayment vừa được ghi nhận
     */
    @PostMapping("/{debtId}/payments")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<DebtPayment>> makePayment(@PathVariable Long debtId, @Valid @RequestBody DebtPayment payment) {
        try {
            DebtPayment createdPayment = debtService.makePayment(debtId, payment);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment made successfully", createdPayment));
        } catch (Exception e) {
            log.error("Error making payment for debt {}", debtId, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to make payment: " + e.getMessage(), null));
        }
    }

    // ==========================================================
    // =============== READ OPERATIONS ==========================
    // ==========================================================

    /**
     * Lấy danh sách tất cả các khoản nợ trong hệ thống
     * URL: GET /api/debts
     * Quyền: ADMIN, EVM_STAFF
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<Debt>>> getAllDebts() {
        List<Debt> debts = debtService.getAllDebts();
        return ResponseEntity.ok(new ApiResponse<>(true, "All debts retrieved successfully", debts));
    }

    /**
     * Lấy danh sách nợ theo dealer
     * URL: GET /api/debts/dealer/{dealerId}
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     */
    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Debt>>> getDebtsByDealer(@PathVariable Long dealerId) {
        List<Debt> debts = debtService.getDebtsByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debts for dealer retrieved successfully", debts));
    }

    /**
     * Lấy danh sách nợ theo customer
     * URL: GET /api/debts/customer/{customerId}
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Debt>>> getDebtsByCustomer(@PathVariable Long customerId) {
        List<Debt> debts = debtService.getDebtsByCustomer(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debts for customer retrieved successfully", debts));
    }

    /**
     * Lấy danh sách nợ theo user
     * URL: GET /api/debts/user/{userId}
     * Quyền: DEALER_STAFF, DEALER_MANAGER
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Debt>>> getDebtsByUser(@PathVariable Long userId) {
        List<Debt> debts = debtService.getDebtsByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debts for user retrieved successfully", debts));
    }

    /**
     * Lấy danh sách nợ theo trạng thái (status)
     * URL: GET /api/debts/status/{status}
     * Quyền: ADMIN, EVM_STAFF
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<Debt>>> getDebtsByStatus(@PathVariable String status) {
        List<Debt> debts = debtService.getDebtsByStatus(status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debts by status retrieved successfully", debts));
    }

    /**
     * Lấy danh sách nợ quá hạn (overdue) theo dealer
     * URL: GET /api/debts/overdue/{dealerId}
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     */
    @GetMapping("/overdue/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Debt>>> getOverdueDebts(@PathVariable Long dealerId) {
        List<Debt> debts = debtService.getOverdueDebts(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Overdue debts retrieved successfully", debts));
    }

    /**
     * Lấy chi tiết một khoản nợ theo ID
     * URL: GET /api/debts/{id}
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Debt>> getDebtById(@PathVariable Long id) {
        Debt debt = debtService.getDebtById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt retrieved successfully", debt));
    }

    /**
     * Lấy danh sách lịch trả nợ (DebtSchedule) theo ID khoản nợ
     * URL: GET /api/debts/{id}/schedules
     */
    @GetMapping("/{id}/schedules")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DebtSchedule>>> getDebtSchedules(@PathVariable Long id) {
        List<DebtSchedule> schedules = debtService.getDebtSchedules(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt schedules retrieved successfully", schedules));
    }

    /**
     * Lấy danh sách các khoản thanh toán (DebtPayment) theo ID khoản nợ
     * URL: GET /api/debts/{id}/payments
     */
    @GetMapping("/{id}/payments")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DebtPayment>>> getDebtPayments(@PathVariable Long id) {
        List<DebtPayment> payments = debtService.getDebtPayments(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt payments retrieved successfully", payments));
    }

    // ==========================================================
    // =============== UPDATE OPERATIONS ========================
    // ==========================================================

    /**
     * Cập nhật trạng thái (status) của một khoản nợ
     * URL: PUT /api/debts/{id}/status?status=...&notes=...
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Debt>> updateDebtStatus(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String notes) {
        Debt updatedDebt = debtService.updateDebtStatus(id, status, notes);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt status updated successfully", updatedDebt));
    }

    // ==========================================================
    // =============== DELETE OPERATIONS ========================
    // ==========================================================

    /**
     * Xóa một khoản nợ khỏi hệ thống
     * URL: DELETE /api/debts/{id}
     * Quyền: ADMIN, EVM_STAFF
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteDebt(@PathVariable Long id) {
        debtService.deleteDebt(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt deleted successfully", null));
    }

    // ==========================================================
    // =============== STATISTICS (THỐNG KÊ) ====================
    // ==========================================================

    /**
     * Lấy thống kê nợ của một dealer (ví dụ: tổng nợ, nợ đã trả, nợ còn lại, v.v.)
     * URL: GET /api/debts/dealer/{dealerId}/stats
     */
    @GetMapping("/dealer/{dealerId}/stats")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDebtStats(@PathVariable Long dealerId) {
        Map<String, Object> stats = debtService.getDebtStats(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt statistics retrieved successfully", stats));
    }

    /**
     * Lấy tổng số tiền nợ chưa thanh toán (outstanding) của một dealer
     * URL: GET /api/debts/dealer/{dealerId}/outstanding
     */
    @GetMapping("/dealer/{dealerId}/outstanding")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Double>> getTotalOutstanding(@PathVariable Long dealerId) {
        Double total = debtService.getTotalOutstandingByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Total outstanding amount retrieved successfully", total));
    }

    /**
     * Lấy tổng số tiền nợ chưa thanh toán của một khách hàng
     * URL: GET /api/debts/customer/{customerId}/outstanding
     */
    @GetMapping("/customer/{customerId}/outstanding")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Double>> getCustomerOutstanding(@PathVariable Long customerId) {
        Double total = debtService.getTotalOutstandingByCustomer(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer outstanding amount retrieved successfully", total));
    }

    /**
     * Lấy danh sách lịch trả nợ (DebtSchedule) bị quá hạn của một dealer
     * URL: GET /api/debts/overdue-schedules/{dealerId}
     */
    @GetMapping("/overdue-schedules/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DebtSchedule>>> getOverdueSchedules(@PathVariable Long dealerId) {
        List<DebtSchedule> schedules = debtService.getOverdueSchedules(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Overdue schedules retrieved successfully", schedules));
    }
}
